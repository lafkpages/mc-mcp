package luisafk.mcmcp.tools.inventory;

import static luisafk.mcmcp.Client.MC;

import java.util.Map;

import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import luisafk.mcmcp.tools.BaseTool;
import net.minecraft.entity.player.PlayerInventory;

public class SetSelectedItemTool extends BaseTool {

    public String getName() {
        return "set_selected_item";
    }

    public String getDescription() {
        return "Set the selected item in the player's hand";
    }

    public String getArgumentsSchema() {
        return """
                {
                    "type": "object",
                    "properties": {
                        "slotIndex": {
                            "type": "integer",
                            "description": "The slot number (0-8) to set as the selected item in the player's hand.",
                            "minimum": 0,
                            "maximum": 8
                        }
                    },
                    "required": ["slotIndex"]
                }
                """;
    }

    public CallToolResult execute(Object exchange, Map<String, Object> arguments) {
        int slotIndex = ((Number) arguments.get("slotIndex")).intValue();

        PlayerInventory inventory = MC.player.getInventory();

        inventory.setSelectedSlot(slotIndex);

        return new CallToolResult("Selected item set to slot " + slotIndex, false);
    }
}
