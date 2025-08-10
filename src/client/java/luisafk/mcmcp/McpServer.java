package luisafk.mcmcp;

import static luisafk.mcmcp.Client.LOGGER;
import static luisafk.mcmcp.Client.MOD_ID;
import static luisafk.mcmcp.Client.MOD_VERSION;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.HttpServletSseServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema.ServerCapabilities;
import luisafk.mcmcp.advisors.AdvisorRegistry;
import luisafk.mcmcp.tools.ToolRegistry;

public class McpServer {
    private Server server;
    private HttpServletSseServerTransportProvider transportProvider;
    private McpSyncServer mcpServer;

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
        mcpServer = io.modelcontextprotocol.server.McpServer.sync(transportProvider)
                .serverInfo(MOD_ID, MOD_VERSION)
                .capabilities(ServerCapabilities.builder()
                        .tools(true) // Enable tools
                        .logging() // Enable logging
                        .build())
                .build();

        // Register tools BEFORE starting the server
        ToolRegistry.registerAllTools(mcpServer);
        AdvisorRegistry.initAll();

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

    public void stop() {
        LOGGER.info("Stopping MCP Server...");

        if (mcpServer != null) {
            mcpServer.closeGracefully();
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
