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

    public boolean start() {
        server = new Server(25567);
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        // Create the transport provider
        ObjectMapper mapper = new ObjectMapper();
        transportProvider = new HttpServletSseServerTransportProvider(mapper, "/mcp/message");

        // Create the MCP server
        mcpServer = io.modelcontextprotocol.server.McpServer.async(transportProvider)
                .serverInfo(MOD_ID, MOD_VERSION)
                .capabilities(ServerCapabilities.builder()
                        .tools(true) // Enable tools
                        .build())
                .build();

        // Use a single, more robust servlet mapping
        // The transport provider will handle routing /mcp/message and /mcp/sse
        // internally.
        context.addServlet(new ServletHolder(transportProvider), "/mcp/*");

        // Start the server
        try {
            server.start();
            System.out.println("MCP Server started on port 25567");
        } catch (Exception e) {
            e.printStackTrace();
            return false; // Exit if server fails to start
        }

        // Add your tools
        registerTools();

        return true;
    }

    private void registerTools() {
        mcpServer.addTool(new McpServerFeatures.AsyncToolSpecification(
                new Tool("get_player_position", "Get the player's current position", "{ \"type\": \"object\" }"),
                (exchange, arguments) -> {
                    LOGGER.info("Tool 'get_player_position' called");
                    return Mono.just(
                            new CallToolResult(
                                    MC.player == null ? "Player not found" : MC.player.getPos().toString(),
                                    MC.player == null));
                })).subscribe();
    }

    public void stop() {
        if (mcpServer != null) {
            mcpServer.closeGracefully().block();
        }

        if (server != null) {
            try {
                server.stop();
                server.join();
            } catch (Exception e) {
                e.printStackTrace();
                // TODO: idk what to do if server stop fails lmao
            }
        }
    }
}
