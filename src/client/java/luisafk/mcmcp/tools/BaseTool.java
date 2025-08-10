package luisafk.mcmcp.tools;

import static luisafk.mcmcp.Client.MC;

import java.util.Map;

import org.jetbrains.annotations.ApiStatus.NonExtendable;

import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.Content;
import luisafk.mcmcp.advisors.AdvisorRegistry;

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

    @NonExtendable
    public CallToolResult handler(Object exchange, Map<String, Object> arguments) {
        if (MC.world == null) {
            return new CallToolResult("World not found - not in game", true);
        }

        if (MC.player == null) {
            return new CallToolResult("Player is null, might not be in-game", true);
        }

        CallToolResult toolResult = execute(exchange, arguments);

        CallToolResult.Builder builder = CallToolResult
                .builder()
                .isError(toolResult.isError());

        for (Content content : toolResult.content()) {
            builder.addContent(content);
        }

        for (Content content : AdvisorRegistry.getAll()) {
            builder.addContent(content);
        }

        return builder.build();
    }
}
