package luisafk.mcmcp.tools;

import static luisafk.mcmcp.Client.MC;

import java.util.Map;

import io.modelcontextprotocol.spec.McpSchema.CallToolResult;

public abstract class BaseTool {

    public abstract String getName();

    public abstract String getDescription();

    /**
     * Defaults to an empty object schema.
     */
    public String getArgumentsSchema() {
        return """
                {
                    "type": "object",
                    "properties": {},
                    "required": []
                }
                """;
    }

    public void init() {
    }

    public abstract CallToolResult execute(Object exchange, Map<String, Object> arguments);

    public CallToolResult handler(Object exchange, Map<String, Object> arguments) {
        CallToolResult result = execute(exchange, arguments);

        // TODO: append notifications

        return result;
    }

    protected boolean isPlayerAvailable() {
        return MC.player != null;
    }

    protected boolean isWorldAvailable() {
        return MC.world != null;
    }

    protected CallToolResult playerNotFoundError() {
        return new CallToolResult("Player not found - not in game", true);
    }

    protected CallToolResult worldNotFoundError() {
        return new CallToolResult("World not found - not in game", true);
    }

    protected CallToolResult worldOrPlayerNotFoundError() {
        return new CallToolResult("World or player not found - not in game", true);
    }

}
