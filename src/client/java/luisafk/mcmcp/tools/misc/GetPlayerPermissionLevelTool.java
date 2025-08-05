package luisafk.mcmcp.tools.misc;

import static luisafk.mcmcp.Client.MC;

import java.util.Map;

import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import luisafk.mcmcp.tools.BaseTool;

public class GetPlayerPermissionLevelTool extends BaseTool {
    public String getName() {
        return "get_player_permission_level";
    }

    public String getDescription() {
        return "Get the player's permission/op level (0-4) in the current world/server.";
    }

    public CallToolResult execute(Object exchange, Map<String, Object> arguments) {
        // See https://minecraft.fandom.com/wiki/Permission_level
        return new CallToolResult("Player permission/op level: " + MC.player.getPermissionLevel(), false);
    }
}
