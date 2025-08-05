package luisafk.mcmcp.tools.world;

import static luisafk.mcmcp.Client.MC;

import java.util.Map;

import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import luisafk.mcmcp.tools.BaseTool;

public class GetWorldWeatherTool extends BaseTool {

    public String getName() {
        return "get_world_weather";
    }

    public String getDescription() {
        return "Get the current world weather";
    }

    public CallToolResult execute(Object exchange, Map<String, Object> arguments) {
        String weather;
        if (MC.world.isThundering()) {
            weather = "thunder";
        } else if (MC.world.isRaining()) {
            weather = "rain";
        } else {
            weather = "clear";
        }

        return new CallToolResult(
                String.format("Weather: %s", weather),
                false);
    }
}
