package luisafk.mcmcp.tools.world;

import static luisafk.mcmcp.Client.MC;

import java.util.Map;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.Tool;
import luisafk.mcmcp.tools.BaseTool;

public class GetWorldTimeTool extends BaseTool {

    @Override
    public McpServerFeatures.SyncToolSpecification create() {
        return new McpServerFeatures.SyncToolSpecification(
                new Tool("get_world_time", "Get the current world time of day", EMPTY_ARGUMENTS_SCHEMA),
                this::execute);
    }

    private CallToolResult execute(Object exchange, Map<String, Object> arguments) {
        if (!isWorldAvailable()) {
            return worldNotFoundError();
        }

        return new CallToolResult(
                String.format("Time of day: %d (ticks)", MC.world.getTimeOfDay() % 24000),
                false);
    }
}
