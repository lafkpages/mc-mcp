package luisafk.mcmcp.tools.baritone;

import static luisafk.mcmcp.Client.IS_BARITONE_INSTALLED;
import static luisafk.mcmcp.Client.MC;

import java.util.Map;

import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import luisafk.mcmcp.tools.BaseTool;

public class BaritoneGotoTool extends BaseTool {

    public String getName() {
        return "baritone_goto";
    }

    public String getDescription() {
        return "Uses Baritone to go to a specified location. Internally uses the Baritone #goto command.";
    }

    @Override
    public String getArgumentsSchema() {
        return """
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
    }

    public CallToolResult execute(Object exchange, Map<String, Object> arguments) {
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
