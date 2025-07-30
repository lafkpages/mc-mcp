package luisafk.mcmcp.tools.world;

import static luisafk.mcmcp.Client.MC;

import java.util.List;
import java.util.Map;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.Tool;
import luisafk.mcmcp.tools.BaseTool;

public class GetNearbyEntitiesTool extends BaseTool {

    private static final String SCHEMA = """
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

    @Override
    public McpServerFeatures.SyncToolSpecification create() {
        return new McpServerFeatures.SyncToolSpecification(
                new Tool("get_nearby_entities", "Get a list of entities near the player within a given radius", SCHEMA),
                this::execute);
    }

    private CallToolResult execute(Object exchange, Map<String, Object> arguments) {
        if (!isPlayerAvailable() || !isWorldAvailable()) {
            return worldOrPlayerNotFoundError();
        }

        double radius = ((Number) arguments.getOrDefault("radius", 10.0)).doubleValue();
        double radiusSquared = radius * radius;

        List<String> entityList = java.util.stream.StreamSupport
                .stream(MC.world.getEntities().spliterator(), false)
                .filter(e -> !e.equals(MC.player) && e.squaredDistanceTo(MC.player) <= radiusSquared)
                .sorted((e1, e2) -> Double.compare(e1.squaredDistanceTo(MC.player),
                        e2.squaredDistanceTo(MC.player)))
                .map(e -> {
                    double ex = e.getX();
                    double ey = e.getY();
                    double ez = e.getZ();
                    double distance = Math.sqrt(e.squaredDistanceTo(MC.player));

                    return String.format("- %s (%s) at [%.1f, %.1f, %.1f] - %.1f blocks away",
                            e.getName().getString(),
                            e.getType().toString(),
                            ex, ey, ez,
                            distance);
                })
                .toList();

        if (entityList.isEmpty()) {
            return new CallToolResult("No entities found within a " + radius + " block radius", false);
        }

        String result = String.format("Entities within a %.1f block radius:\n%s", radius,
                String.join("\n", entityList));

        return new CallToolResult(result, false);
    }
}
