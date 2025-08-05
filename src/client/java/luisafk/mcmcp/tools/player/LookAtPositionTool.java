package luisafk.mcmcp.tools.player;

import static luisafk.mcmcp.Client.MC;

import java.util.Map;

import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import luisafk.mcmcp.tools.BaseTool;
import net.minecraft.util.math.Vec3d;

public class LookAtPositionTool extends BaseTool {

    public String getName() {
        return "look_at_position";
    }

    public String getDescription() {
        return "Make the player look at a given position. Useful in combination with the `use_item_in_hand_on_targeted_block` tool to, for example, place blocks.";
    }

    public String getArgumentsSchema() {
        return """
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
                        },
                        "lookAtBlockCenter": {
                            "type": "boolean",
                            "description": "If true (default), the player will look at the center of the block at the specified coordinates. If false, the player will look at the exact coordinates.",
                            "default": true
                        }
                    },
                    "required": ["x", "y", "z"]
                }
                """;
    }

    public CallToolResult execute(Object exchange, Map<String, Object> arguments) {
        double targetX = ((Number) arguments.get("x")).doubleValue();
        double targetY = ((Number) arguments.get("y")).doubleValue();
        double targetZ = ((Number) arguments.get("z")).doubleValue();
        boolean lookAtBlockCenter = (boolean) arguments.getOrDefault("lookAtBlockCenter", true);

        if (lookAtBlockCenter) {
            // Adjust the target position to the center of the block
            targetX = targetX > 0 ? Math.floor(targetX) + 0.5 : Math.ceil(targetX) - 0.5;
            targetY = targetY > 0 ? Math.floor(targetY) + 0.5 : Math.ceil(targetY) - 0.5;
            targetZ = targetZ > 0 ? Math.floor(targetZ) + 0.5 : Math.ceil(targetZ) - 0.5;
        }

        Vec3d playerPos = MC.player.getEyePos();
        Vec3d targetPos = new Vec3d(targetX, targetY, targetZ);
        Vec3d direction = targetPos.subtract(playerPos).normalize();

        // Calculate yaw and pitch
        double yaw = Math.toDegrees(Math.atan2(-direction.x, direction.z));
        double pitch = Math.toDegrees(Math.asin(-direction.y));

        // Set the player's rotation
        MC.player.setYaw((float) yaw);
        MC.player.setPitch((float) pitch);

        return new CallToolResult(
                String.format("Player is now looking at position [%.1f, %.1f, %.1f]", targetX, targetY, targetZ),
                false);
    }
}
