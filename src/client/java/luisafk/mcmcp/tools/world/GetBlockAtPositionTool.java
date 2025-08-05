package luisafk.mcmcp.tools.world;

import static luisafk.mcmcp.Client.MC;

import java.util.Map;

import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import luisafk.mcmcp.tools.BaseTool;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public class GetBlockAtPositionTool extends BaseTool {

    public String getName() {
        return "get_block_at_position";
    }

    public String getDescription() {
        return "Get information about the block at a specific position";
    }

    public String getArgumentsSchema() {
        return """
                {
                    "type": "object",
                    "properties": {
                        "x": {
                            "type": "number",
                            "description": "X coordinate of the block position"
                        },
                        "y": {
                            "type": "number",
                            "description": "Y coordinate of the block position"
                        },
                        "z": {
                            "type": "number",
                            "description": "Z coordinate of the block position"
                        }
                    },
                    "required": ["x", "y", "z"]
                }
                """;
    }

    public CallToolResult execute(Object exchange, Map<String, Object> arguments) {
        if (!isWorldAvailable()) {
            return worldNotFoundError();
        }

        double x = ((Number) arguments.get("x")).doubleValue();
        double y = ((Number) arguments.get("y")).doubleValue();
        double z = ((Number) arguments.get("z")).doubleValue();

        BlockPos pos = new BlockPos((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z));
        BlockState blockState = MC.world.getBlockState(pos);

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Block at position [%d, %d, %d]:\n", pos.getX(), pos.getY(), pos.getZ()));
        sb.append(String.format("Block type: %s\n", blockState.getBlock().toString()));
        sb.append(String.format("Block state: %s\n", blockState.toString()));
        sb.append(String.format("Display name: %s\n", blockState.getBlock().getName().getString()));

        // Additional block properties
        sb.append(String.format("Is air: %s\n", blockState.isAir()));
        sb.append(String.format("Is solid: %s\n", blockState.isSolidBlock(MC.world, pos)));
        sb.append(String.format("Is transparent: %s\n", !blockState.isOpaque()));
        sb.append(String.format("Hardness: %.1f\n", blockState.getHardness(MC.world, pos)));

        // Light properties
        sb.append(String.format("Light level: %d\n", MC.world.getLightLevel(pos)));
        sb.append(String.format("Block light: %d\n", MC.world.getLightLevel(net.minecraft.world.LightType.BLOCK, pos)));
        sb.append(String.format("Sky light: %d", MC.world.getLightLevel(net.minecraft.world.LightType.SKY, pos)));

        return new CallToolResult(sb.toString(), false);
    }
}
