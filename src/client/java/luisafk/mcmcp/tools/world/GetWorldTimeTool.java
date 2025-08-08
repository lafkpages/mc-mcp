package luisafk.mcmcp.tools.world;

import static luisafk.mcmcp.Client.MC;

import java.util.Map;

import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import luisafk.mcmcp.tools.BaseTool;

public class GetWorldTimeTool extends BaseTool {

    public String getName() {
        return "get_world_time";
    }

    public String getDescription() {
        return "Get the current world time of day";
    }

    public CallToolResult execute(Object exchange, Map<String, Object> arguments) {
        int timeOfDay = (int) (MC.world.getTimeOfDay() % 24000);

        return new CallToolResult(
                String.format("""
                        Time of day: %d (ticks)

                        Note that:
                        - 1 tick = 3.6s in Minecraft (50ms in real time)
                        - 1000 ticks = 1 hour in Minecraft (50s in real time)
                        - 24000 ticks = 1 full day in Minecraft (20m in real time)
                        """, timeOfDay),
                false);
    }
}
