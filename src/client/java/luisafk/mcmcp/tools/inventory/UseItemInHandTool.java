package luisafk.mcmcp.tools.inventory;

import static luisafk.mcmcp.Client.MC;

import java.util.Map;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.Tool;
import luisafk.mcmcp.tools.BaseTool;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

public class UseItemInHandTool extends BaseTool {
    @Override
    public McpServerFeatures.SyncToolSpecification create() {
        return new McpServerFeatures.SyncToolSpecification(
                new Tool("use_item_in_hand",
                        "Use the item in the main hand of the player. This tool will not use the off-hand item. This tool should not be used to use items on blocks, for that the `use_item_in_hand_on_targeted_block` tool should be used.",
                        EMPTY_ARGUMENTS_SCHEMA),
                this::execute);
    }

    private CallToolResult execute(Object exchange, Map<String, Object> arguments) {
        if (!isPlayerAvailable()) {
            return playerNotFoundError();
        }

        Hand activeHand = MC.player.getActiveHand();
        ActionResult result = MC.interactionManager.interactItem(
                MC.player,
                activeHand);

        if (result.isAccepted()) {
            MC.player.swingHand(activeHand);
        }

        return new CallToolResult(
                "Item used, got ActionResult: " + result,
                result.equals(ActionResult.FAIL));
    }
}
