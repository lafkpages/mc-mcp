package luisafk.mcmcp.tools.player;

import static luisafk.mcmcp.Client.MC;

import java.util.Map;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.Tool;
import luisafk.mcmcp.tools.BaseTool;

public class GetPlayerBiomeTool extends BaseTool {

    @Override
    public McpServerFeatures.SyncToolSpecification create() {
        return new McpServerFeatures.SyncToolSpecification(
                new Tool("get_player_biome", "Get the biome the player is currently in", EMPTY_ARGUMENTS_SCHEMA),
                this::execute);
    }

    private CallToolResult execute(Object exchange, Map<String, Object> arguments) {
        if (!isWorldAvailable() || !isPlayerAvailable()) {
            return worldOrPlayerNotFoundError();
        }

        return new CallToolResult(MC.world.getBiome(MC.player.getBlockPos()).toString(), false);
    }
}
