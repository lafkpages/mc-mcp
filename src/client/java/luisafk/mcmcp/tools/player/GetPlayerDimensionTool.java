package luisafk.mcmcp.tools.player;

import static luisafk.mcmcp.Client.MC;

import java.util.Map;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.Tool;
import luisafk.mcmcp.tools.BaseTool;

public class GetPlayerDimensionTool extends BaseTool {

    @Override
    public McpServerFeatures.SyncToolSpecification create() {
        return new McpServerFeatures.SyncToolSpecification(
                new Tool("get_player_dimension", "Get the current dimension of the player", EMPTY_ARGUMENTS_SCHEMA),
                this::execute);
    }

    private CallToolResult execute(Object exchange, Map<String, Object> arguments) {
        if (!isWorldAvailable()) {
            return worldNotFoundError();
        }

        return new CallToolResult(
                String.format("Dimension: %s", MC.world.getRegistryKey().getValue()),
                false);
    }
}
