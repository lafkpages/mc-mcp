package luisafk.mcmcp.tools.misc;

import static luisafk.mcmcp.Client.MC;

import java.util.Map;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.Tool;
import luisafk.mcmcp.tools.BaseTool;

public class GetPlayerPermissionLevelTool extends BaseTool {
    @Override
    public McpServerFeatures.SyncToolSpecification create() {
        return new McpServerFeatures.SyncToolSpecification(
                new Tool("get_player_permission_level",
                        "Get the player's permission/op level (0-4) in the current world/server.",
                        EMPTY_ARGUMENTS_SCHEMA),
                this::execute);
    }

    private CallToolResult execute(Object exchange, Map<String, Object> arguments) {
        if (!isPlayerAvailable()) {
            return playerNotFoundError();
        }

        return new CallToolResult("Player permission/op level: " + MC.player.getPermissionLevel(), false);
    }
}
