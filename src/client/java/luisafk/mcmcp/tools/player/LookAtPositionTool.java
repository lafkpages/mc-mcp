package luisafk.mcmcp.tools.player;

import static luisafk.mcmcp.Client.MC;

import java.util.Map;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.Tool;
import luisafk.mcmcp.tools.BaseTool;
import net.minecraft.util.math.Vec3d;

public class LookAtPositionTool extends BaseTool {
    private static final String SCHEMA = """
            {
                "type": "object",
                "properties": {
                    "x": {
                        "type": "number",
                        "description": "The X coordinate to look at."
                    },
                    "y": {
                        "type": "number",
                        "description": "The Y coordinate to look at."
                    },
                    "z": {
                        "type": "number",
                        "description": "The Z coordinate to look at."
                    }
                },
                "required": ["x", "y", "z"]
            }
            """;

    @Override
    public McpServerFeatures.SyncToolSpecification create() {
        return new McpServerFeatures.SyncToolSpecification(
                new Tool("look_at_position",
                        "Make the player look at a given position. Useful in combination with the `use_item_in_hand_on_targeted_block` tool to, for example, place blocks.",
                        SCHEMA),
                this::execute);
    }

    private CallToolResult execute(Object exchange, Map<String, Object> arguments) {
        if (!isPlayerAvailable()) {
            return playerNotFoundError();
        }

        double targetX = ((Number) arguments.get("x")).doubleValue();
        double targetY = ((Number) arguments.get("y")).doubleValue();
        double targetZ = ((Number) arguments.get("z")).doubleValue();

        Vec3d playerPos = MC.player.getEyePos();
        Vec3d targetPos = new Vec3d(targetX, targetY, targetZ);
        Vec3d direction = targetPos.subtract(playerPos).normalize();

        // Calculate yaw and pitch
        double yaw = Math.atan2(-direction.x, direction.z) * 180.0 / Math.PI;
        double pitch = Math.asin(-direction.y) * 180.0 / Math.PI;

        // Set the player's rotation
        MC.player.setYaw((float) yaw);
        MC.player.setPitch((float) pitch);

        return new CallToolResult(
                String.format("Player is now looking at position [%.1f, %.1f, %.1f]", targetX, targetY, targetZ),
                false);
    }
}
