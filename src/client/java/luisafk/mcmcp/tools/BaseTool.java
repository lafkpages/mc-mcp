package luisafk.mcmcp.tools;

import static luisafk.mcmcp.Client.MC;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;

public abstract class BaseTool {

    public abstract McpServerFeatures.SyncToolSpecification create();

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

    protected static final String EMPTY_ARGUMENTS_SCHEMA = """
            { "type": "object" }
            """;
}
