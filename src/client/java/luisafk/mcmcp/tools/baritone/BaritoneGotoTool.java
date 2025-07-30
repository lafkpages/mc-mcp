package luisafk.mcmcp.tools.baritone;

import static luisafk.mcmcp.Client.IS_BARITONE_INSTALLED;
import static luisafk.mcmcp.Client.MC;

import java.util.Map;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.Tool;
import luisafk.mcmcp.tools.BaseTool;

public class BaritoneGotoTool extends BaseTool {
    private static final String SCHEMA = """
            {
                "type": "object",
                "properties": {
                    "x": {
                        "type": "number",
                        "description": "The integer X coordinate to go to."
                    },
                    "y": {
                        "type": "number",
                        "description": "The integer Y coordinate to go to. Optional."
                    },
                    "z": {
                        "type": "number",
                        "description": "The integer Z coordinate to go to."
                    }
                },
                "required": ["x", "z"]
            }
            """;

    @Override
    public McpServerFeatures.SyncToolSpecification create() {
        return new McpServerFeatures.SyncToolSpecification(
                new Tool("baritone_goto",
                        "Uses Baritone to go to a specified location. Internally uses the Baritone #goto command.",
                        SCHEMA),
                this::execute);
    }

    private CallToolResult execute(Object exchange, Map<String, Object> arguments) {
        if (!isPlayerAvailable()) {
            return playerNotFoundError();
        }

        if (!IS_BARITONE_INSTALLED) {
            return new CallToolResult("The Baritone mod is not installed", true);
        }

        int x = ((Number) arguments.get("x")).intValue();
        int z = ((Number) arguments.get("z")).intValue();

        if (arguments.containsKey("y")) {
            int y = ((Number) arguments.get("y")).intValue();

            MC.player.networkHandler.sendChatMessage("#goto " + x + " " + y + " " + z);
        } else {
            MC.player.networkHandler.sendChatMessage("#goto " + x + " " + z);
        }

        return new CallToolResult("Set goal and started pathing successfully", false);
    }
}
