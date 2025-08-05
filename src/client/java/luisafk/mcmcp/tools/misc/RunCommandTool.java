package luisafk.mcmcp.tools.misc;

import static luisafk.mcmcp.Client.MC;

import java.util.Map;

import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import luisafk.mcmcp.tools.BaseTool;

public class RunCommandTool extends BaseTool {

    public String getName() {
        return "run_command";
    }

    public String getDescription() {
        return "Run a Minecraft command as the player. Note that the command's output will not be returned, so running commands such as `locate` here is useless.";
    }

    public String getArgumentsSchema() {
        return """
                {
                    "type": "object",
                    "properties": {
                        "command": {
                            "type": "string",
                            "description": "The command to run, e.g. 'say hello' or 'gamemode creative'. Do not include the leading slash (/)."
                        }
                    },
                    "required": ["command"]
                }
                """;
    }

    public CallToolResult execute(Object exchange, Map<String, Object> arguments) {
        String command = (String) arguments.get("command");

        if (command == null || command.isBlank()) {
            return new CallToolResult("No command provided", true);
        }

        MC.player.networkHandler.sendChatCommand(command);

        return new CallToolResult("Command sent: " + command, false);
    }
}
