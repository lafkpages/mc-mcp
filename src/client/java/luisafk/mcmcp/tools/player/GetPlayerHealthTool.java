package luisafk.mcmcp.tools.player;

import static luisafk.mcmcp.Client.MC;

import java.util.Map;

import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import luisafk.mcmcp.tools.BaseTool;

public class GetPlayerHealthTool extends BaseTool {

    public String getDescription() {
        return "Get the current health of the player";
    }

    public CallToolResult execute(Object exchange, Map<String, Object> arguments) {
        return new CallToolResult(
                String.format("Health: %.1f / %.1f", MC.player.getHealth(), MC.player.getMaxHealth()),
                false);
    }
}
