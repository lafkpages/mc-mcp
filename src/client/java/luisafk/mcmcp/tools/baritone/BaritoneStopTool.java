package luisafk.mcmcp.tools.baritone;

import static luisafk.mcmcp.Client.IS_BARITONE_INSTALLED;
import static luisafk.mcmcp.Client.MC;

import java.util.Map;

import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import luisafk.mcmcp.tools.BaseTool;

public class BaritoneStopTool extends BaseTool {
    public String getName() {
        return "baritone_stop";
    }

    public String getDescription() {
        return "Stops current Baritone processes such as mining via the mine_all tool or pathing via the goto tool.";
    }

    public CallToolResult execute(Object exchange, Map<String, Object> arguments) {
        if (!IS_BARITONE_INSTALLED) {
            return new CallToolResult("The Baritone mod is not installed", true);
        }

        MC.player.networkHandler.sendChatMessage("#stop");
        return new CallToolResult("Sent Baritone #stop command", false);
    }
}
