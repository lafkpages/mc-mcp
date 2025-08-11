package luisafk.mcmcp.tools.player;

import static luisafk.mcmcp.Client.MC;

import java.util.Map;

import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import luisafk.mcmcp.tools.BaseTool;

public class GetPlayerHungerTool extends BaseTool {

    public String getDescription() {
        return "Get the current hunger/food level of the player";
    }

    public CallToolResult execute(Object exchange, Map<String, Object> arguments) {
        // TODO: is it always really out of 20?
        return new CallToolResult(
                String.format("Hunger: %d / 20", MC.player.getHungerManager().getFoodLevel()),
                false);
    }
}
