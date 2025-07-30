package luisafk.mcmcp.tools.world;

import static luisafk.mcmcp.Client.MC;

import java.util.Map;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.Tool;
import luisafk.mcmcp.tools.BaseTool;

public class GetWorldWeatherTool extends BaseTool {

    @Override
    public McpServerFeatures.SyncToolSpecification create() {
        return new McpServerFeatures.SyncToolSpecification(
                new Tool("get_world_weather", "Get the current world weather", EMPTY_ARGUMENTS_SCHEMA),
                this::execute);
    }

    private CallToolResult execute(Object exchange, Map<String, Object> arguments) {
        if (!isWorldAvailable()) {
            return worldNotFoundError();
        }

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
