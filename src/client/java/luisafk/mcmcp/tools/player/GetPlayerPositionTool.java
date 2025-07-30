package luisafk.mcmcp.tools.player;

import static luisafk.mcmcp.Client.MC;

import java.util.Map;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.Tool;
import luisafk.mcmcp.tools.BaseTool;

public class GetPlayerPositionTool extends BaseTool {

    @Override
    public McpServerFeatures.SyncToolSpecification create() {
        return new McpServerFeatures.SyncToolSpecification(
                new Tool("get_player_position", "Get the current position of the player", EMPTY_ARGUMENTS_SCHEMA),
                this::execute);
    }

    private CallToolResult execute(Object exchange, Map<String, Object> arguments) {
        if (!isPlayerAvailable()) {
            return playerNotFoundError();
        }

        return new CallToolResult(
                String.format("X: %.2f, Y: %.2f, Z: %.2f",
                        MC.player.getX(),
                        MC.player.getY(),
                        MC.player.getZ()),
                false);
    }
}
