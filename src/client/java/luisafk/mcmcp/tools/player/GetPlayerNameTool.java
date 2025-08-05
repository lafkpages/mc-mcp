package luisafk.mcmcp.tools.player;

import static luisafk.mcmcp.Client.MC;

import java.util.Map;

import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import luisafk.mcmcp.tools.BaseTool;

public class GetPlayerNameTool extends BaseTool {

    public String getName() {
        return "get_player_name";
    }

    public String getDescription() {
        return "Get the current player's name";
    }

    public CallToolResult execute(Object exchange, Map<String, Object> arguments) {
        return new CallToolResult(
                String.format("Player name: %s", MC.player.getName().getString()),
                false);
    }
}
