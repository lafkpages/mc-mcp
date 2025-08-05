package luisafk.mcmcp.tools.world;

import static luisafk.mcmcp.Client.MC;

import java.util.HashMap;
import java.util.Map;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.Tool;
import luisafk.mcmcp.tools.BaseTool;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public class GetNearbyBlocksTool extends BaseTool {
    private static final String SCHEMA = """
            {
                "type": "object",
                "properties": {
                    "radius": {
                        "type": "number",
                        "description": "Radius around the player to check for block types (in blocks).",
                        "default": 5
                    }
                },
                "required": []
            }
            """;

    @Override
    public McpServerFeatures.SyncToolSpecification create() {
        return new McpServerFeatures.SyncToolSpecification(
                new Tool("get_nearby_blocks",
                        "Get the unique block types (with state) and their counts within a given radius of the player",
                        SCHEMA),
                this::execute);
    }

    private CallToolResult execute(Object exchange, Map<String, Object> arguments) {
        if (!isPlayerAvailable() || !isWorldAvailable()) {
            return worldOrPlayerNotFoundError();
        }

        double radius = ((Number) arguments.getOrDefault("radius", 5.0)).doubleValue();
        int iradius = (int) Math.ceil(radius);

        BlockPos playerPos = MC.player.getBlockPos();

        Map<String, Integer> blockCounts = new HashMap<>();

        int checked = 0;
        for (int dx = -iradius; dx <= iradius; dx++) {
            for (int dy = -iradius; dy <= iradius; dy++) {
                for (int dz = -iradius; dz <= iradius; dz++) {
                    double distSq = dx * dx + dy * dy + dz * dz;

                    if (distSq > radius * radius) {
                        continue;
                    }

                    BlockPos pos = playerPos.add(dx, dy, dz);
                    BlockState state = MC.world.getBlockState(pos);
                    String blockStateString = state.toString();

                    blockCounts.put(blockStateString, blockCounts.getOrDefault(blockStateString, 0) + 1);

                    checked++;
                }
            }
        }

        if (blockCounts.isEmpty()) {
            return new CallToolResult("No blocks found within a " + radius + " block radius", false);
        }

        StringBuilder sb = new StringBuilder();
        sb.append(
                String.format("Block types (with state) and counts within %.1f blocks of player (checked %d blocks):\n",
                        radius, checked));

        // Sort by count (descending) for better readability
        blockCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(entry -> {
                    sb.append(String.format("- %s: %d blocks\n", entry.getKey(), entry.getValue()));
                });

        return new CallToolResult(sb.toString(), false);
    }
}
