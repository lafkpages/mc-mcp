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
                        "default": 500
                    },
                    "filter": {
                        "type": "string",
                        "description": "Optional filter to search for entities by name or type. Case-insensitive partial matching.",
                        "default": ""
                    }
                },
                "required": []
            }
            """;

    @Override
    public McpServerFeatures.SyncToolSpecification create() {
        return new McpServerFeatures.SyncToolSpecification(
                new Tool("get_nearby_entities",
                        "Get a list of entities near the player within a given radius, optionally filtered by name or type",
                        SCHEMA),
                this::execute);
    }

    private static class EntityInfo {
        final String name;
        final String type;
        final double x, y, z, distance;

        EntityInfo(String name, String type, double x, double y, double z, double distance) {
            this.name = name;
            this.type = type;
            this.x = x;
            this.y = y;
            this.z = z;
            this.distance = distance;
        }
    }

    private CallToolResult execute(Object exchange, Map<String, Object> arguments) {
        if (!isPlayerAvailable() || !isWorldAvailable()) {
            return worldOrPlayerNotFoundError();
        }

        double radius = ((Number) arguments.getOrDefault("radius", 500.0)).doubleValue();
        String filter = ((String) arguments.getOrDefault("filter", "")).toLowerCase().trim();
        double radiusSquared = radius * radius;

        List<EntityInfo> entityInfos = new java.util.ArrayList<>();
        for (var e : MC.world.getEntities()) {
            if (e.equals(MC.player)) {
                continue;
            }

            double distSq = e.squaredDistanceTo(MC.player);

            if (distSq > radiusSquared) {
                continue;
            }

            String entityName = e.getName().getString().toLowerCase();
            String entityType = e.getType().toString().toLowerCase();

            // Apply filter if provided
            if (!filter.isEmpty()) {
                if (!entityName.contains(filter) && !entityType.contains(filter)) {
                    continue;
                }
            }

            entityInfos.add(new EntityInfo(
                    e.getName().getString(),
                    e.getType().toString(),
                    e.getX(), e.getY(), e.getZ(),
                    Math.sqrt(distSq)));
        }

        if (entityInfos.isEmpty()) {
            String message = filter.isEmpty()
                    ? "No entities found within a " + radius + " block radius"
                    : "No entities matching '" + filter + "' found within a " + radius + " block radius";
            return new CallToolResult(message, false);
        }

        entityInfos.sort(java.util.Comparator.comparingDouble(info -> info.distance));

        StringBuilder sb = new StringBuilder();
        if (filter.isEmpty()) {
            sb.append(String.format("Entities within a %.1f block radius:\n", radius));
        } else {
            sb.append(String.format("Entities matching '%s' within a %.1f block radius:\n", filter, radius));
        }

        for (EntityInfo info : entityInfos) {
            sb.append(String.format("- %s (%s) at [%.1f, %.1f, %.1f] - %.1f blocks away\n",
                    info.name, info.type, info.x, info.y, info.z, info.distance));
        }

        return new CallToolResult(sb.toString().trim(), false);
    }
}
