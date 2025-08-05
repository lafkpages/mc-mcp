package luisafk.mcmcp.tools.inventory;

import static luisafk.mcmcp.Client.MC;

import java.util.Map;

import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import luisafk.mcmcp.tools.BaseTool;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;

public class GetSelectedItemTool extends BaseTool {

    public String getName() {
        return "get_selected_item";
    }

    public String getDescription() {
        return "Get information about the currently selected item in the player's hand";
    }

    public CallToolResult execute(Object exchange, Map<String, Object> arguments) {
        if (!isPlayerAvailable()) {
            return playerNotFoundError();
        }

        PlayerInventory inventory = MC.player.getInventory();
        int selectedSlot = inventory.getSelectedSlot();
        ItemStack selectedItem = inventory.getStack(selectedSlot);

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Selected slot: %d\n", selectedSlot));

        if (selectedItem.isEmpty()) {
            sb.append("Selected item: Empty (no item in hand)");
        } else {
            sb.append(String.format("Selected item: %s\n", selectedItem.getItem().toString()));
            sb.append(String.format("Display name: %s\n", selectedItem.getName().getString()));
            sb.append(String.format("Count: %d\n", selectedItem.getCount()));
            sb.append(String.format("Max stack size: %d\n", selectedItem.getMaxCount()));

            if (selectedItem.isDamageable()) {
                sb.append(String.format("Durability: %d/%d\n",
                        selectedItem.getMaxDamage() - selectedItem.getDamage(),
                        selectedItem.getMaxDamage()));
            } else {
                sb.append("Durability: N/A (item is not damageable)\n");
            }

            if (selectedItem.hasEnchantments()) {
                sb.append("Enchantments: ");
                sb.append(selectedItem.getEnchantments().toString());
            }
        }

        return new CallToolResult(sb.toString(), false);
    }
}
