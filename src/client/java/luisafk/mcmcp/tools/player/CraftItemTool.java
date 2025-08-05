package luisafk.mcmcp.tools.player;

import static luisafk.mcmcp.Client.MC;

import java.util.List;
import java.util.Map;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.Tool;
import luisafk.mcmcp.tools.BaseTool;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;

public class CraftItemTool extends BaseTool {

    private static final String SCHEMA = """
            {
                "type": "object",
                "properties": {
                    "recipe": {
                        "type": "array",
                        "description": "Crafting grid represented as an array of item names. Use null for empty slots. Items are arranged left-to-right, top-to-bottom. For 2x2 crafting (player inventory), provide 4 items. For 3x3 crafting (crafting table), provide 9 items.",
                        "items": {
                            "type": ["string", "null"]
                        },
                        "minItems": 4,
                        "maxItems": 9
                    },
                    "count": {
                        "type": "number",
                        "description": "Number of items to craft (will attempt multiple crafting operations if needed)",
                        "default": 1,
                        "minimum": 1
                    }
                },
                "required": ["recipe"]
            }
            """;

    @Override
    public McpServerFeatures.SyncToolSpecification create() {
        return new McpServerFeatures.SyncToolSpecification(
                new Tool("craft_item",
                        "Craft an item using the player's inventory (2x2) or crafting table (3x3). Recipe size determines which interface to use.",
                        SCHEMA),
                this::execute);
    }

    private CallToolResult execute(Object exchange, Map<String, Object> arguments) {
        if (!isPlayerAvailable() || !isWorldAvailable()) {
            return worldOrPlayerNotFoundError();
        }

        @SuppressWarnings("unchecked")
        List<String> recipePattern = (List<String>) arguments.get("recipe");
        int requestedCount = ((Number) arguments.getOrDefault("count", 1)).intValue();

        if (recipePattern == null || (recipePattern.size() != 4 && recipePattern.size() != 9)) {
            return new CallToolResult(
                    "Recipe must be an array of exactly 4 items (2x2 player crafting) or 9 items (3x3 crafting table)",
                    true);
        }

        try {
            ScreenHandler screenHandler = MC.player.currentScreenHandler;
            boolean usingCraftingTable = screenHandler instanceof CraftingScreenHandler;
            boolean usingPlayerInventory = screenHandler instanceof PlayerScreenHandler;

            // Validate recipe size matches available crafting interface
            if (recipePattern.size() == 9) {
                // 3x3 recipe requires crafting table
                if (!usingCraftingTable) {
                    return new CallToolResult(
                            "3x3 recipe requires a crafting table. Please open a crafting table first.", true);
                }
            } else if (recipePattern.size() == 4) {
                // 2x2 recipe requires player inventory
                if (!usingPlayerInventory) {
                    return new CallToolResult(
                            "2x2 recipe requires player inventory crafting. Please close the crafting table first.",
                            true);
                }
            }

            if (!usingCraftingTable && !usingPlayerInventory) {
                return new CallToolResult(
                        "No crafting interface available. Open a crafting table or use player inventory.", true);
            }

            int craftedCount = 0;
            StringBuilder resultMessage = new StringBuilder();

            for (int attempt = 0; attempt < requestedCount; attempt++) {
                if (attemptCraft(MC.player, recipePattern, usingCraftingTable)) {
                    craftedCount++;
                } else {
                    break; // Stop if we can't craft anymore
                }
            }

            if (craftedCount > 0) {
                resultMessage.append("Successfully crafted ").append(craftedCount).append(" item(s)");

                if (craftedCount < requestedCount) {
                    resultMessage.append(" (requested ").append(requestedCount).append(" but insufficient materials)");
                }

                return new CallToolResult(resultMessage.toString(), true);
            } else {
                return new CallToolResult(
                        "Failed to craft item. Check that you have the required materials and the recipe is correct.",
                        true);
            }

        } catch (Exception e) {
            return new CallToolResult("Error during crafting: " + e.getMessage(), true);
        }
    }

    private boolean attemptCraft(ClientPlayerEntity player, List<String> recipePattern, boolean usingCraftingTable) {
        try {
            ScreenHandler screenHandler = player.currentScreenHandler;

            // Clear crafting grid first
            clearCraftingGrid(screenHandler, usingCraftingTable);

            // Place items in crafting grid
            if (!placeItemsInCraftingGrid(screenHandler, recipePattern, usingCraftingTable)) {
                return false;
            }

            // Check if recipe is valid and take result
            return takeCraftingResult(screenHandler, usingCraftingTable);

        } catch (Exception e) {
            return false;
        }
    }

    private void clearCraftingGrid(ScreenHandler screenHandler, boolean usingCraftingTable) {
        int craftingStart = usingCraftingTable ? 1 : 1; // Crafting slots start at index 1
        int craftingSize = usingCraftingTable ? 9 : 4; // 3x3 for table, 2x2 for player

        for (int i = 0; i < craftingSize; i++) {
            int slotIndex = craftingStart + i;
            if (!screenHandler.getSlot(slotIndex).getStack().isEmpty()) {
                // Shift-click to move to inventory
                MC.interactionManager.clickSlot(
                        screenHandler.syncId,
                        slotIndex,
                        0,
                        SlotActionType.QUICK_MOVE,
                        MC.player);
            }
        }
    }

    private boolean placeItemsInCraftingGrid(ScreenHandler screenHandler, List<String> recipePattern,
            boolean usingCraftingTable) {
        int craftingStart = usingCraftingTable ? 1 : 1;
        int gridWidth = usingCraftingTable ? 3 : 2;
        int gridHeight = usingCraftingTable ? 3 : 2;
        int expectedRecipeSize = gridWidth * gridHeight;

        // Validate recipe size matches the crafting interface
        if (recipePattern.size() != expectedRecipeSize) {
            return false;
        }

        for (int i = 0; i < recipePattern.size(); i++) {
            String itemName = recipePattern.get(i);

            if (itemName != null && !itemName.trim().isEmpty()) {
                // Find item in player inventory
                int inventorySlot = findItemInInventory(screenHandler, itemName);

                if (inventorySlot == -1) {
                    return false; // Don't have required item
                }

                int craftingSlot = craftingStart + i;

                // Move one item from inventory to crafting grid
                MC.interactionManager.clickSlot(
                        screenHandler.syncId,
                        inventorySlot,
                        0,
                        SlotActionType.PICKUP,
                        MC.player);

                MC.interactionManager.clickSlot(
                        screenHandler.syncId,
                        craftingSlot,
                        1, // Right click to place one item
                        SlotActionType.PICKUP,
                        MC.player);

                // Put remaining items back
                MC.interactionManager.clickSlot(
                        screenHandler.syncId,
                        inventorySlot,
                        0,
                        SlotActionType.PICKUP,
                        MC.player);
            }
        }

        return true;
    }

    private boolean takeCraftingResult(ScreenHandler screenHandler, boolean usingCraftingTable) {
        int resultSlot = 0; // Result slot is always 0

        ItemStack result = screenHandler.getSlot(resultSlot).getStack();

        if (result.isEmpty()) {
            return false; // No valid recipe
        }

        // Take the crafted item
        MC.interactionManager.clickSlot(
                screenHandler.syncId,
                resultSlot,
                0,
                SlotActionType.QUICK_MOVE,
                MC.player);

        return true;
    }

    private int findItemInInventory(ScreenHandler screenHandler, String itemName) {
        // Search in player inventory slots
        int inventoryStart = screenHandler instanceof CraftingScreenHandler ? 10 : 9;
        int inventoryEnd = inventoryStart + 36; // 36 inventory slots

        for (int i = inventoryStart; i < inventoryEnd; i++) {
            ItemStack stack = screenHandler.getSlot(i).getStack();

            if (!stack.isEmpty()) {
                String stackItemName = stack.getItem().toString();

                // Try different matching strategies
                if (stackItemName.contains(itemName.toLowerCase()) ||
                        itemName.toLowerCase().contains(stackItemName.toLowerCase()) ||
                        stack.getItem().getName().getString().toLowerCase().contains(itemName.toLowerCase())) {
                    return i;
                }
            }
        }

        return -1; // Item not found
    }
}