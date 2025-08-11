package luisafk.mcmcp.tools.player;

import static luisafk.mcmcp.Client.MC;

import java.util.Map;

import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import luisafk.mcmcp.tools.BaseTool;

public class GetPlayerDimensionTool extends BaseTool {

    public String getDescription() {
        return "Get the current dimension of the player";
    }

    public CallToolResult execute(Object exchange, Map<String, Object> arguments) {
        return new CallToolResult(
                String.format("Dimension: %s", MC.world.getRegistryKey().getValue()),
                false);
    }
}
