package luisafk.mcmcp.tools.world;

import static luisafk.mcmcp.Client.MC;

import java.util.Map;

import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import luisafk.mcmcp.tools.BaseTool;

public class GetWorldTimeTool extends BaseTool {

    public String getName() {
        return "get_world_time";
    }

    public String getDescription() {
        return "Get the current world time of day";
    }

    public CallToolResult execute(Object exchange, Map<String, Object> arguments) {
        return new CallToolResult(
                String.format("Time of day: %d (ticks)", MC.world.getTimeOfDay() % 24000),
                false);
    }
}
