package luisafk.mcmcp;

import static luisafk.mcmcp.Client.LOGGER;
import static luisafk.mcmcp.Client.MC;
import static luisafk.mcmcp.Client.MOD_ID;
import static luisafk.mcmcp.Client.MOD_VERSION;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.modelcontextprotocol.server.McpAsyncServer;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.transport.HttpServletSseServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.ServerCapabilities;
import io.modelcontextprotocol.spec.McpSchema.Tool;
import reactor.core.publisher.Mono;

public class McpServer {
    private Server server;
    private HttpServletSseServerTransportProvider transportProvider;
    private McpAsyncServer mcpServer;

    public void start() {
        LOGGER.info("Starting MCP Server...");

        server = new Server(25567);
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        // Create the transport provider
        ObjectMapper mapper = new ObjectMapper();
        transportProvider = new HttpServletSseServerTransportProvider(mapper, "/mcp/message");

        // Create the MCP server with logging
        mcpServer = io.modelcontextprotocol.server.McpServer.async(transportProvider)
                .serverInfo(MOD_ID, MOD_VERSION)
                .capabilities(ServerCapabilities.builder()
                        .tools(true) // Enable tools
                        .logging() // Enable logging
                        .build())
                .build();

        // Register tools BEFORE starting the server
        registerTools();

        // Register the servlet
        context.addServlet(new ServletHolder(transportProvider), "/mcp/*");

        // Start the server
        try {
            server.start();
        } catch (Exception e) {
            LOGGER.error("Failed to start MCP Server", e);
            return;
        }

        LOGGER.info("MCP Server started on port 25567");
        LOGGER.info("SSE endpoint: http://localhost:25567/mcp/sse");
        LOGGER.info("Message endpoint: http://localhost:25567/mcp/message");
    }

    private void registerTools() {
        String emptySchema = """
                { "type": "object" }
                """;

        // Block on registration to ensure it completes
        mcpServer.addTool(new McpServerFeatures.AsyncToolSpecification(
                new Tool("get_player_position", "Get the current position of the player",
                        emptySchema),
                (exchange, arguments) -> {
                    if (MC.player == null) {
                        return Mono.just(new CallToolResult("Player not found - not in game",
                                true));
                    }

                    return Mono.just(new CallToolResult(String.format("X: %.2f, Y: %.2f, Z: %.2f",
                            MC.player.getX(),
                            MC.player.getY(),
                            MC.player.getZ()), false));
                }))
                .doOnError(e -> LOGGER.error("Failed to register tool", e))
                .block(); // Important: block to ensure registration completes
    }

    public void stop() {
        LOGGER.info("Stopping MCP Server...");

        if (mcpServer != null) {
            mcpServer.closeGracefully().block();
        }

        if (server != null) {
            try {
                server.stop();
                server.join();
                LOGGER.info("MCP Server stopped");
            } catch (Exception e) {
                LOGGER.error("Error stopping server", e);
            }
        }
    }
}
