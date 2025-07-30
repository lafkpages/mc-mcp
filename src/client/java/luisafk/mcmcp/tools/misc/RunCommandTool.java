package luisafk.mcmcp.tools.misc;

import static luisafk.mcmcp.Client.MC;

import java.util.Map;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.Tool;
import luisafk.mcmcp.tools.BaseTool;

public class RunCommandTool extends BaseTool {
    private static final String SCHEMA = """
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

    @Override
    public McpServerFeatures.SyncToolSpecification create() {
        return new McpServerFeatures.SyncToolSpecification(
                new Tool("run_command", "Run a Minecraft command as the player.", SCHEMA),
                this::execute);
    }

    private CallToolResult execute(Object exchange, Map<String, Object> arguments) {
        if (!isPlayerAvailable()) {
            return playerNotFoundError();
        }

        String command = (String) arguments.get("command");

        if (command == null || command.isBlank()) {
            return new CallToolResult("No command provided", true);
        }

        MC.player.networkHandler.sendChatCommand(command);

        return new CallToolResult("Command sent: " + command, false);
    }
}
