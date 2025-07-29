package luisafk.mcmcp;

import static luisafk.mcmcp.Client.LOGGER;
import static luisafk.mcmcp.Client.MC;
import static luisafk.mcmcp.Client.MOD_ID;
import static luisafk.mcmcp.Client.MOD_VERSION;

import java.util.List;
import java.util.UUID;

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
import net.minecraft.client.network.AbstractClientPlayerEntity;
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

        // Important: block on registration to ensure it completes

        // Important: tool descriptions must not contain apostrophes (') or they will
        // not work in Raycast.

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
                .block();

        mcpServer.addTool(new McpServerFeatures.AsyncToolSpecification(
                new Tool("get_player_biome", "Get the biome the player is currently in",
                        emptySchema),
                (exchange, arguments) -> {
                    if (MC.world == null || MC.player == null) {
                        return Mono.just(new CallToolResult("World or player not found - not in game",
                                true));
                    }

                    return Mono.just(new CallToolResult(MC.world.getBiome(MC.player.getBlockPos()).toString(), false));
                }))
                .doOnError(e -> LOGGER.error("Failed to register tool", e))
                .block();

        mcpServer.addTool(new McpServerFeatures.AsyncToolSpecification(
                new Tool("get_player_dimension", "Get the current dimension of the player", emptySchema),
                (exchange, arguments) -> {
                    if (MC.world == null) {
                        return Mono.just(new CallToolResult("World not found - not in game", true));
                    }

                    return Mono.just(new CallToolResult(
                            String.format("Dimension: %s", MC.world.getRegistryKey().getValue()), false));
                }))
                .doOnError(e -> LOGGER.error("Failed to register tool", e))
                .block();

        mcpServer.addTool(new McpServerFeatures.AsyncToolSpecification(
                new Tool("get_player_health", "Get the current health of the player", emptySchema),
                (exchange, arguments) -> {
                    if (MC.player == null) {
                        return Mono.just(new CallToolResult("Player not found - not in game", true));
                    }

                    return Mono.just(new CallToolResult(
                            String.format("Health: %.1f / %.1f", MC.player.getHealth(), MC.player.getMaxHealth()),
                            false));
                }))
                .doOnError(e -> LOGGER.error("Failed to register tool", e))
                .block();

        mcpServer.addTool(new McpServerFeatures.AsyncToolSpecification(
                new Tool("get_player_hunger", "Get the current hunger/food level of the player", emptySchema),
                (exchange, arguments) -> {
                    if (MC.player == null) {
                        return Mono.just(new CallToolResult("Player not found - not in game", true));
                    }

                    // TODO: is it always really out of 20?
                    return Mono.just(new CallToolResult(
                            String.format("Hunger: %d / 20", MC.player.getHungerManager().getFoodLevel()), false));
                }))
                .doOnError(e -> LOGGER.error("Failed to register tool", e))
                .block();

        mcpServer.addTool(new McpServerFeatures.AsyncToolSpecification(
                new Tool("get_world_time", "Get the current world time of day", emptySchema),
                (exchange, arguments) -> {
                    if (MC.world == null) {
                        return Mono.just(new CallToolResult("World not found - not in game", true));
                    }

                    return Mono.just(new CallToolResult(
                            String.format("Time of day: %d (ticks)", MC.world.getTimeOfDay() % 24000), false));
                }))
                .doOnError(e -> LOGGER.error("Failed to register tool", e))
                .block();

        mcpServer.addTool(new McpServerFeatures.AsyncToolSpecification(
                new Tool("get_world_weather", "Get the current world weather", emptySchema),
                (exchange, arguments) -> {
                    if (MC.world == null) {
                        return Mono.just(new CallToolResult("World not found - not in game", true));
                    }

                    String weather;
                    if (MC.world.isThundering()) {
                        weather = "thunder";
                    } else if (MC.world.isRaining()) {
                        weather = "rain";
                    } else {
                        weather = "clear";
                    }

                    return Mono.just(new CallToolResult(
                            String.format("Weather: %s", weather), false));
                }))
                .doOnError(e -> LOGGER.error("Failed to register tool", e))
                .block();

        mcpServer.addTool(new McpServerFeatures.AsyncToolSpecification(
                new Tool("get_player_name", "Get the current player's name", emptySchema),
                (exchange, arguments) -> {
                    if (MC.player == null) {
                        return Mono.just(new CallToolResult("Player not found - not in game", true));
                    }

                    return Mono.just(new CallToolResult(
                            String.format("Player name: %s", MC.player.getName().getString()), false));
                }))
                .doOnError(e -> LOGGER.error("Failed to register tool", e))
                .block();

        mcpServer.addTool(new McpServerFeatures.AsyncToolSpecification(
                new Tool("list_online_players", "Get the list of online players (excluding the current player)",
                        emptySchema),
                (exchange, arguments) -> {
                    if (MC.world == null || MC.player == null) {
                        return Mono.just(new CallToolResult("World or player not found - not in game", true));
                    }

                    List<AbstractClientPlayerEntity> players = MC.world.getPlayers();
                    UUID currentPlayerUuid = MC.player.getUuid();

                    String playersList = "";

                    for (AbstractClientPlayerEntity player : players) {
                        if (!player.getUuid().equals(currentPlayerUuid)) {
                            if (!playersList.isEmpty()) {
                                playersList += ", ";
                            }
                            playersList += player.getName().getString();
                        }
                    }

                    if (playersList.isEmpty()) {
                        playersList = "No other players online";
                    }

                    return Mono.just(new CallToolResult(
                            String.format("Other online players (%d excluding you): %s",
                                    players.size() - 1,
                                    playersList),
                            false));
                }))
                .doOnError(e -> LOGGER.error("Failed to register tool", e))
                .block();
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
