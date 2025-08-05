package luisafk.mcmcp.tools.player;

import static luisafk.mcmcp.Client.MC;

import java.util.Map;

import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import luisafk.mcmcp.tools.BaseTool;

public class GetPlayerPositionTool extends BaseTool {

    public String getName() {
        return "get_player_position";
    }

    public String getDescription() {
        return "Get the current position of the player";
    }

    public CallToolResult execute(Object exchange, Map<String, Object> arguments) {
        return new CallToolResult(
                String.format("X: %.2f, Y: %.2f, Z: %.2f",
                        MC.player.getX(),
                        MC.player.getY(),
                        MC.player.getZ()),
                false);
    }
}
