package luisafk.mcmcp.tools.player;

import static luisafk.mcmcp.Client.MC;

import java.util.Map;

import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import luisafk.mcmcp.tools.BaseTool;

public class GetPlayerBiomeTool extends BaseTool {

    public String getName() {
        return "get_player_biome";
    }

    public String getDescription() {
        return "Get the biome the player is currently in";
    }

    public CallToolResult execute(Object exchange, Map<String, Object> arguments) {
        return new CallToolResult(MC.world.getBiome(MC.player.getBlockPos()).toString(), false);
    }
}
