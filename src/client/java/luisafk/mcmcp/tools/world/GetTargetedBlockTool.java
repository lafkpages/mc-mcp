package luisafk.mcmcp.tools.world;

import static luisafk.mcmcp.Client.MC;

import java.util.Map;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.Tool;
import luisafk.mcmcp.tools.BaseTool;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

public class GetTargetedBlockTool extends BaseTool {

    private static final String SCHEMA = """
            {
                "type": "object",
                "properties": {
                    "includeFluids": {
                        "type": "boolean",
                        "description": "Whether to include fluids in the raycast. If true, the tool will return the fluid block if the player is looking at one. If false, it will only return solid blocks, ignoring fluids if there are any.",
                        "default": false
                    }
                },
                "required": []
            }
            """;

    @Override
    public McpServerFeatures.SyncToolSpecification create() {
        return new McpServerFeatures.SyncToolSpecification(
                new Tool("get_targeted_block", "Get the block the player is looking at", SCHEMA),
                this::execute);
    }

    private CallToolResult execute(Object exchange, Map<String, Object> arguments) {
        if (!isPlayerAvailable() || !isWorldAvailable()) {
            return worldOrPlayerNotFoundError();
        }

        HitResult hit = MC.player.raycast(MC.player.getBlockInteractionRange(), 0,
                (Boolean) arguments.getOrDefault("includeFluids", false));

        if (hit instanceof BlockHitResult) {
            BlockPos blockPos = ((BlockHitResult) hit).getBlockPos();

            return new CallToolResult(
                    String.format("Targeted block: %s at %s",
                            MC.world.getBlockState(blockPos).toString(),
                            blockPos.toShortString()),
                    false);
        }

        return new CallToolResult(
                String.format("No block targeted (hit type %s)", hit.getType().toString()),
                false);
    }
}
