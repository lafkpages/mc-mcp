package luisafk.mcmcp.tools.baritone;

import static luisafk.mcmcp.Client.IS_BARITONE_INSTALLED;
import static luisafk.mcmcp.Client.MC;

import java.util.List;
import java.util.Map;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.Tool;
import luisafk.mcmcp.tools.BaseTool;

public class BaritoneMineTool extends BaseTool {
    private static final String SCHEMA = """
            {
                "type": "object",
                "properties": {
                    "blocks": {
                        "type": "array",
                        "items": {
                            "type": "string"
                        },
                        "description": "A list of block names to mine (e.g., 'diamond_ore', 'iron_ore')."
                    }
                },
                "required": ["blocks"]
            }
            """;

    @Override
    public McpServerFeatures.SyncToolSpecification create() {
        return new McpServerFeatures.SyncToolSpecification(
                new Tool("baritone_mine_all",
                        "Uses Baritone to mine all blocks of the given types indefinitely. This tool will keep mining until stopped or no more of the specified blocks can be found. Internally uses the Baritone #mine command.",
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

        Object blocksObj = arguments.get("blocks");

        if (!(blocksObj instanceof List<?> blocks) || blocks.isEmpty()) {
            return new CallToolResult("No blocks specified to mine", true);
        }

        StringBuilder command = new StringBuilder("#mine");
        for (Object block : blocks) {
            command.append(" ").append(block.toString());
        }

        MC.player.networkHandler.sendChatMessage(command.toString());
        return new CallToolResult("Started mining (ran Baritone command: " + command + ")", false);
    }
}
