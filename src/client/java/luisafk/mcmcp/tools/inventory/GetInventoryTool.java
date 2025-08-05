package luisafk.mcmcp.tools.inventory;

import static luisafk.mcmcp.Client.MC;
import static net.minecraft.entity.player.PlayerInventory.HOTBAR_SIZE;

import java.util.List;
import java.util.Map;

import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import luisafk.mcmcp.tools.BaseTool;
import net.minecraft.item.ItemStack;

public class GetInventoryTool extends BaseTool {

    public String getName() {
        return "get_inventory";
    }

    public String getDescription() {
        return "Get the items in the inventory of the player";
    }

    public String getArgumentsSchema() {
        return """
                {
                    "type": "object",
                    "properties": {
                        "hotbarOnly": {
                            "type": "boolean",
                            "description": "If true, only returns the items in the hotbar (first 9 slots). If false (default), returns all items in the inventory.",
                            "default": false
                        }
                    }
                }
                """;
    }

    public CallToolResult execute(Object exchange, Map<String, Object> arguments) {
        if (!isPlayerAvailable()) {
            return playerNotFoundError();
        }

        List<ItemStack> itemStacks = MC.player.getInventory().getMainStacks();
        StringBuilder itemsString = new StringBuilder();

        Boolean hotbarOnly = (Boolean) arguments.getOrDefault("hotbarOnly", false);

        if (hotbarOnly) {
            itemsString.append("Hotbar items:");
        } else {
            itemsString.append("Inventory items:");
        }

        for (int i = 0; i < (hotbarOnly ? HOTBAR_SIZE : itemStacks.size()); i++) {
            itemsString.append(String.format("\nSlot %d: ", i));

            ItemStack item = itemStacks.get(i);

            if (item.isEmpty()) {
                itemsString.append("<empty slot>");
                continue;
            }

            itemsString.append(item.toString());
        }

        return new CallToolResult(itemsString.toString(), false);
    }
}
