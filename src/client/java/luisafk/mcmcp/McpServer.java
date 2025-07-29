package luisafk.mcmcp;

import static luisafk.mcmcp.Client.IS_BARITONE_INSTALLED;
import static luisafk.mcmcp.Client.LOGGER;
import static luisafk.mcmcp.Client.MC;
import static luisafk.mcmcp.Client.MOD_ID;
import static luisafk.mcmcp.Client.MOD_VERSION;
import static net.minecraft.entity.player.PlayerInventory.HOTBAR_SIZE;

import java.util.List;
import java.util.UUID;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.HttpServletSseServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.ServerCapabilities;
import io.modelcontextprotocol.spec.McpSchema.Tool;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

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
        String emptyArgumentsSchema = """
                { "type": "object" }
                """;

        // Important: block on registration to ensure it completes

        // Important: tool descriptions must not contain apostrophes (') or they will
        // not work in Raycast.

        mcpServer.addTool(new McpServerFeatures.SyncToolSpecification(
                new Tool("get_player_position", "Get the current position of the player",
                        emptyArgumentsSchema),
                (exchange, arguments) -> {
                    if (MC.player == null) {
                        return new CallToolResult("Player not found - not in game", true);
                    }

                    return new CallToolResult(String.format("X: %.2f, Y: %.2f, Z: %.2f",
                            MC.player.getX(),
                            MC.player.getY(),
                            MC.player.getZ()),
                            false);
                }));

        mcpServer.addTool(new McpServerFeatures.SyncToolSpecification(
                new Tool("get_player_biome", "Get the biome the player is currently in",
                        emptyArgumentsSchema),
                (exchange, arguments) -> {
                    if (MC.world == null || MC.player == null) {
                        return new CallToolResult("World or player not found - not in game", true);
                    }

                    return new CallToolResult(MC.world.getBiome(MC.player.getBlockPos()).toString(), false);
                }));

        mcpServer.addTool(new McpServerFeatures.SyncToolSpecification(
                new Tool("get_player_dimension", "Get the current dimension of the player", emptyArgumentsSchema),
                (exchange, arguments) -> {
                    if (MC.world == null) {
                        return new CallToolResult("World not found - not in game", true);
                    }

                    return new CallToolResult(
                            String.format("Dimension: %s", MC.world.getRegistryKey().getValue()),
                            false);
                }));

        mcpServer.addTool(new McpServerFeatures.SyncToolSpecification(
                new Tool("get_player_health", "Get the current health of the player", emptyArgumentsSchema),
                (exchange, arguments) -> {
                    if (MC.player == null) {
                        return new CallToolResult("Player not found - not in game", true);
                    }

                    return new CallToolResult(
                            String.format("Health: %.1f / %.1f", MC.player.getHealth(), MC.player.getMaxHealth()),
                            false);
                }));

        mcpServer.addTool(new McpServerFeatures.SyncToolSpecification(
                new Tool("get_player_hunger", "Get the current hunger/food level of the player", emptyArgumentsSchema),
                (exchange, arguments) -> {
                    if (MC.player == null) {
                        return new CallToolResult("Player not found - not in game", true);
                    }

                    // TODO: is it always really out of 20?
                    return new CallToolResult(
                            String.format("Hunger: %d / 20", MC.player.getHungerManager().getFoodLevel()),
                            false);
                }));

        mcpServer.addTool(new McpServerFeatures.SyncToolSpecification(
                new Tool("get_world_time", "Get the current world time of day", emptyArgumentsSchema),
                (exchange, arguments) -> {
                    if (MC.world == null) {
                        return new CallToolResult("World not found - not in game", true);
                    }

                    return new CallToolResult(
                            String.format("Time of day: %d (ticks)", MC.world.getTimeOfDay() % 24000),
                            false);
                }));

        mcpServer.addTool(new McpServerFeatures.SyncToolSpecification(
                new Tool("get_world_weather", "Get the current world weather", emptyArgumentsSchema),
                (exchange, arguments) -> {
                    if (MC.world == null) {
                        return new CallToolResult("World not found - not in game", true);
                    }

                    String weather;
                    if (MC.world.isThundering()) {
                        weather = "thunder";
                    } else if (MC.world.isRaining()) {
                        weather = "rain";
                    } else {
                        weather = "clear";
                    }

                    return new CallToolResult(
                            String.format("Weather: %s", weather),
                            false);
                }));

        mcpServer.addTool(new McpServerFeatures.SyncToolSpecification(
                new Tool("get_player_name", "Get the current player's name", emptyArgumentsSchema),
                (exchange, arguments) -> {
                    if (MC.player == null) {
                        return new CallToolResult("Player not found - not in game", true);
                    }

                    return new CallToolResult(
                            String.format("Player name: %s", MC.player.getName().getString()),
                            false);
                }));

        mcpServer.addTool(new McpServerFeatures.SyncToolSpecification(
                new Tool("list_online_players", "Get the list of online players (excluding the current player)",
                        emptyArgumentsSchema),
                (exchange, arguments) -> {
                    if (MC.world == null || MC.player == null) {
                        return new CallToolResult("World or player not found - not in game", true);
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

                    return new CallToolResult(
                            String.format("Other online players (%d excluding you): %s",
                                    players.size() - 1,
                                    playersList),
                            false);
                }));

        String getTargetedBlockArgumentsSchema = """
                {
                    "type": "object",
                    "properties": {
                        "includeFluids": {
                            "type": "boolean",
                            "description": "Whether to include fluids in the raycast. If true, the tool will return the fluid block if the player is looking at one. If false, it will only return solid blocks, ignoring fluids if there are any.",
                            "default": false
                        }
                    },
                    "required": []
                }
                """;

        mcpServer.addTool(new McpServerFeatures.SyncToolSpecification(
                new Tool("get_targeted_block", "Get the block the player is looking at",
                        getTargetedBlockArgumentsSchema),
                (exchange, arguments) -> {
                    if (MC.player == null || MC.world == null) {
                        return new CallToolResult("Player or world not found - not in game", true);
                    }

                    HitResult hit = MC.player.raycast(MC.player.getBlockInteractionRange(), 0,
                            (Boolean) arguments.getOrDefault("includeFluids", false));

                    if (hit instanceof BlockHitResult) {
                        BlockPos blockPos = ((BlockHitResult) hit).getBlockPos();

                        return new CallToolResult(
                                String.format("Targeted block: %s at %s",
                                        MC.world.getBlockState(blockPos).toString(),
                                        blockPos.toShortString()),
                                false);
                    }

                    return new CallToolResult(
                            String.format("No block targeted (hit type %s)", hit.getType().toString()),
                            false);
                }));

        String getNearbyEntitiesArgumentsSchema = """
                {
                    "type": "object",
                    "properties": {
                        "radius": {
                            "type": "number",
                            "description": "Radius around the player to check for entities (in blocks). Less than 100 is considered close, more than 1000 is considered far.",
                            "default": 10
                        }
                    },
                    "required": []
                }
                """;

        mcpServer.addTool(new McpServerFeatures.SyncToolSpecification(
                new Tool("get_nearby_entities",
                        "Get a list of entities near the player within a given radius",
                        getNearbyEntitiesArgumentsSchema),
                (exchange, arguments) -> {
                    if (MC.player == null || MC.world == null) {
                        return new CallToolResult("Player or world not found - not in game", true);
                    }

                    double radius = ((Number) arguments.getOrDefault("radius", 10.0)).doubleValue();
                    double radiusSquared = radius * radius;

                    List<String> entityList = java.util.stream.StreamSupport
                            .stream(MC.world.getEntities().spliterator(), false)
                            .filter(e -> !e.equals(MC.player) && e.squaredDistanceTo(MC.player) <= radiusSquared)
                            .sorted((e1, e2) -> Double.compare(e1.squaredDistanceTo(MC.player),
                                    e2.squaredDistanceTo(MC.player)))
                            .map(e -> {
                                double distance = Math.sqrt(e.squaredDistanceTo(MC.player));
                                return String.format("- %s (%s) - %.1f blocks away",
                                        e.getName().getString(),
                                        e.getType().toString(),
                                        distance);
                            })
                            .toList();

                    if (entityList.isEmpty()) {
                        return new CallToolResult("No entities found within a " + radius + " block radius", false);
                    }

                    String result = String.format("Entities within a %.1f block radius:\n%s", radius,
                            String.join("\n", entityList));

                    return new CallToolResult(result, false);
                }));

        String mineAllArgumentsSchema = """
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

        mcpServer.addTool(new McpServerFeatures.SyncToolSpecification(
                new Tool("baritone_mine_all",
                        "Uses Baritone to mine all blocks of the given types indefinitely. This tool will keep mining until stopped or no more of the specified blocks can be found. Internally uses the Baritone #mine command.",
                        mineAllArgumentsSchema),
                (exchange, arguments) -> {
                    if (MC.player == null) {
                        return new CallToolResult("Player not found - not in game", true);
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
                }));

        String baritoneGotoArgumentsSchema = """
                {
                    "type": "object",
                    "properties": {
                        "x": {
                            "type": "number",
                            "description": "The integer X coordinate to go to."
                        },
                        "y": {
                            "type": "number",
                            "description": "The integer Y coordinate to go to. Optional."
                        },
                        "z": {
                            "type": "number",
                            "description": "The integer Z coordinate to go to."
                        }
                    },
                    "required": ["x", "z"]
                }
                """;

        mcpServer.addTool(new McpServerFeatures.SyncToolSpecification(
                new Tool("baritone_goto",
                        "Uses Baritone to go to a specified location. Internally uses the Baritone #goto command.",
                        baritoneGotoArgumentsSchema),
                (exchange, arguments) -> {
                    if (MC.player == null) {
                        return new CallToolResult("Player not found - not in game", true);
                    }

                    if (!IS_BARITONE_INSTALLED) {
                        return new CallToolResult("The Baritone mod is not installed", true);
                    }

                    int x = ((Number) arguments.get("x")).intValue();
                    int z = ((Number) arguments.get("z")).intValue();

                    if (arguments.containsKey("y")) {
                        int y = ((Number) arguments.get("y")).intValue();

                        MC.player.networkHandler.sendChatMessage("#goto " + x + " " + y + " " + z);
                    } else {
                        MC.player.networkHandler.sendChatMessage("#goto " + x + " " + z);
                    }

                    return new CallToolResult("Set goal and started pathing successfully", false);

                    // TODO: use BaritoneAPI

                    // Goal goal;
                    // if (arguments.containsKey("y")) {
                    // int y = ((Number) arguments.get("y")).intValue();
                    // goal = new GoalBlock(x, y, z);
                    // } else {
                    // goal = new GoalXZ(x, z);
                    // }

                    // BaritoneAPI.getProvider().getBaritoneForPlayer(MC.player).getCustomGoalProcess()
                    // .setGoalAndPath(goal);

                    // return Mono.just(new CallToolResult("Set goal and started pathing
                    // successfully", false));
                }));

        mcpServer.addTool(new McpServerFeatures.SyncToolSpecification(
                new Tool("baritone_stop",
                        "Stops current Baritone processes such as mining via the mine_all tool or pathing via the goto tool.",
                        emptyArgumentsSchema),
                (exchange, arguments) -> {
                    if (MC.player == null) {
                        return new CallToolResult("Player not found - not in game", true);
                    }

                    if (!IS_BARITONE_INSTALLED) {
                        return new CallToolResult("The Baritone mod is not installed", true);
                    }

                    MC.player.networkHandler.sendChatMessage("#stop");

                    return new CallToolResult("Sent Baritone #stop command", false);
                }));

        String getInventoryArgumentsSchema = """
                {
                    "type": "object",
                    "properties": {
                        "hotbarOnly": {
                            "type": "boolean",
                            "description": "If true, only returns the items in the hotbar (first 9 slots). If false (default), returns all items in the inventory.",
                            "default": false
                        }
                    }
                }
                """;

        mcpServer.addTool(new McpServerFeatures.SyncToolSpecification(
                new Tool("get_inventory", "Get the items in the inventory of the player", getInventoryArgumentsSchema),
                (exchange, arguments) -> {
                    if (MC.player == null) {
                        return new CallToolResult("Player not found - not in game", true);
                    }

                    List<ItemStack> itemStacks = MC.player.getInventory().getMainStacks();
                    StringBuilder itemsString = new StringBuilder();

                    Boolean hotbarOnly = (Boolean) arguments.getOrDefault("hotbarOnly", false);

                    if (hotbarOnly) {
                        itemsString.append("Hotbar items:");
                    } else {
                        itemsString.append("Inventory items:");
                    }

                    for (int i = 0; i < (hotbarOnly ? HOTBAR_SIZE : itemStacks.size()); i++) {
                        itemsString.append(String.format("\nSlot %d: ", i));

                        ItemStack item = itemStacks.get(i);

                        if (item.isEmpty()) {
                            itemsString.append("<empty slot>");
                            continue;
                        }

                        itemsString.append(item.toString());
                    }

                    return new CallToolResult(itemsString.toString(), false);
                }));

        String setSelectedItemArgumentsSchema = """
                {
                    "type": "object",
                    "properties": {
                        "slotIndex": {
                            "type": "integer",
                            "description": "The slot number (0-8) to set as the selected item in the player's hand.",
                            "minimum": 0,
                            "maximum": 8
                        }
                    },
                    "required": ["slotIndex"]
                }
                """;

        mcpServer.addTool(new McpServerFeatures.SyncToolSpecification(
                new Tool("set_selected_item", "Set the selected item in the player's hand",
                        setSelectedItemArgumentsSchema),
                (exchange, arguments) -> {
                    if (MC.player == null) {
                        return new CallToolResult("Player not found - not in game", true);
                    }

                    int slotIndex = ((Number) arguments.get("slotIndex")).intValue();

                    PlayerInventory inventory = MC.player.getInventory();

                    inventory.setSelectedSlot(slotIndex);

                    return new CallToolResult("Selected item set to slot " + slotIndex, false);
                }));

        mcpServer.addTool(new McpServerFeatures.SyncToolSpecification(
                new Tool("use_item_in_hand_on_targeted_block",
                        "Use the item in the hand of the player on the targeted block",
                        emptyArgumentsSchema),
                (exchange, arguments) -> {
                    if (MC.player == null) {
                        return new CallToolResult("Player not found - not in game", true);
                    }

                    HitResult hit = MC.player.raycast(MC.player.getBlockInteractionRange(), 0,
                            (Boolean) arguments.getOrDefault("includeFluids", false));

                    if (!(hit instanceof BlockHitResult)) {
                        return new CallToolResult(
                                "Not targeting a block (raycast hit type: " + hit.getType() + ")",
                                true);
                    }

                    Hand activeHand = MC.player.getActiveHand();
                    ActionResult result = MC.interactionManager.interactBlock(
                            MC.player,
                            activeHand,
                            (BlockHitResult) hit);

                    if (result == ActionResult.SUCCESS) {
                        MC.player.swingHand(activeHand);
                    }

                    return new CallToolResult(
                            "Item used, got ActionResult: " + result,
                            result.equals(ActionResult.FAIL));
                }));

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
