package luisafk.mcmcp.tools.inventory;

import static luisafk.mcmcp.Client.MC;

import java.util.Map;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.Tool;
import luisafk.mcmcp.tools.BaseTool;
import net.minecraft.entity.player.PlayerInventory;

public class SetSelectedItemTool extends BaseTool {

    private static final String SCHEMA = """
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

    @Override
    public McpServerFeatures.SyncToolSpecification create() {
        return new McpServerFeatures.SyncToolSpecification(
                new Tool("set_selected_item", "Set the selected item in the player's hand", SCHEMA),
                this::execute);
    }

    private CallToolResult execute(Object exchange, Map<String, Object> arguments) {
        if (!isPlayerAvailable()) {
            return playerNotFoundError();
        }

        int slotIndex = ((Number) arguments.get("slotIndex")).intValue();

        PlayerInventory inventory = MC.player.getInventory();

        inventory.setSelectedSlot(slotIndex);

        return new CallToolResult("Selected item set to slot " + slotIndex, false);
    }
}
