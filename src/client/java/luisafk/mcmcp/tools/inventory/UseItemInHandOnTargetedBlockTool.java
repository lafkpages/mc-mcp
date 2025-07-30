package luisafk.mcmcp.tools.inventory;

import static luisafk.mcmcp.Client.MC;

import java.util.Map;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.Tool;
import luisafk.mcmcp.tools.BaseTool;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;

public class UseItemInHandOnTargetedBlockTool extends BaseTool {
    private static final String SCHEMA = """
            {
                "type": "object",
                "properties": {
                    "includeFluids": {
                        "type": "boolean",
                        "description": "Whether to include fluids in the raycast. If true (default), the tool will use the item on fluid block if the player is looking at one. If false, it will only use the item on solid blocks, ignoring fluids if there are any.",
                        "default": true
                    }
                },
                "required": []
            }
            """;

    @Override
    public McpServerFeatures.SyncToolSpecification create() {
        return new McpServerFeatures.SyncToolSpecification(
                new Tool("use_item_in_hand_on_targeted_block",
                        "Use the item in the hand of the player on the targeted block",
                        SCHEMA),
                this::execute);
    }

    private CallToolResult execute(Object exchange, Map<String, Object> arguments) {
        if (!isPlayerAvailable()) {
            return playerNotFoundError();
        }

        HitResult hit = MC.player.raycast(MC.player.getBlockInteractionRange(), 0,
                (Boolean) arguments.getOrDefault("includeFluids", true));

        if (!(hit instanceof BlockHitResult)) {
            return new CallToolResult(
                    "Not targeting a block (raycast hit type: " + hit.getType() + ")",
                    true);
        }

        Hand activeHand = MC.player.getActiveHand();
        ActionResult result = MC.interactionManager.interactBlock(
                MC.player,
                activeHand,
                (BlockHitResult) hit);

        if (result == ActionResult.SUCCESS) {
            MC.player.swingHand(activeHand);
        }

        return new CallToolResult(
                "Item used, got ActionResult: " + result,
                result.equals(ActionResult.FAIL));
    }
}
