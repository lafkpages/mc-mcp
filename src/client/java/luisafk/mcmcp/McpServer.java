package luisafk.mcmcp;

import static luisafk.mcmcp.Client.CONFIG;
import static luisafk.mcmcp.Client.LOGGER;
import static luisafk.mcmcp.Client.MOD_ID;
import static luisafk.mcmcp.Client.MOD_VERSION;
import static luisafk.mcmcp.tools.ToolRegistry.TOOLS;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.jetbrains.annotations.NotNull;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.HttpServletSseServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema.ServerCapabilities;
import io.modelcontextprotocol.spec.McpSchema.Tool;
import luisafk.mcmcp.advisors.AdvisorRegistry;
import luisafk.mcmcp.tools.BaseTool;

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

        // Register enabled tools (runs init on all tools enabled or not)
        initialRegisterEnabledTools();

        // Register advisors
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

    public void initialRegisterEnabledTools() {
        TOOLS.forEach((toolName, tool) -> {
            tool.init();

            if (!CONFIG.isToolEnabled(toolName)) {
                return;
            }

            registerTool(toolName, tool);
        });
    }

    public void registerTool(@NotNull String toolName, @NotNull BaseTool tool) {
        LOGGER.info("Registering tool: " + toolName);

        mcpServer.addTool(
                new SyncToolSpecification(
                        new Tool(
                                toolName,
                                tool.getDescription(),
                                tool.getArgumentsSchema()),
                        tool::handler));
    }

    public void unregisterTool(String toolName) {
        LOGGER.info("Unregistering tool: " + toolName);
        mcpServer.removeTool(toolName);
    }
}
