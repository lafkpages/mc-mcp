package luisafk.mcmcp.tools.world;

import static luisafk.mcmcp.Client.MC;

import java.util.Map;

import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import luisafk.mcmcp.tools.BaseTool;

public class GetWorldTimeTool extends BaseTool {

    /** A compact model for notable times during the day */
    private static final class DayEvent {
        final int tick; // tick-of-day [0, 23999]
        final String info;

        DayEvent(int tick, String info) {
            this.tick = tick;
            this.info = info;
        }
    }

    /**
     * @see https://minecraft.fandom.com/wiki/Daylight_cycle#24-hour_Minecraft_day
     */
    private static final DayEvent[] DAY_EVENTS = {
            new DayEvent(0,
                    "**Beginning of the Minecraft day.**\nPlayers and villagers awaken and rise from their beds."),
            new DayEvent(167, "The moon disappears on the horizon."),
            new DayEvent(1000, "Time when using the `/time set day` command."),
            new DayEvent(2000, "Villagers begin their workday."),
            new DayEvent(5723, "The clock starts showing exactly midday."),
            new DayEvent(6000, "**Noon**, the sun is at its peak.\nTime when using `/time set noon`."),
            new DayEvent(9000, "Villagers end their workday and begin socializing."),
            new DayEvent(11834, "The moon appears on the horizon."),
            new DayEvent(12000, "**Beginning of the Minecraft sunset.**\nVillagers go to their beds and sleep."),
            new DayEvent(12010,
                    "In rainy weather, beds can be used at this point.\n\nIn rainy weather, the internal sky-light level begins to decrease."),
            new DayEvent(12040, "In sunny weather, the internal sky-light level begins to decrease."),
            new DayEvent(12542,
                    "In clear weather, beds can be used at this point, bees enter the nest/hive for the night, and undead mobs no longer burn."),
            new DayEvent(12610, "The clock shows exactly dusk (day to night)."),
            new DayEvent(12786, "The solar zenith angle is 0. Horizon stops getting darker."),
            new DayEvent(12969, "First tick when monsters spawn outdoors in rainy weather."),
            new DayEvent(13000,
                    "**Beginning of the Minecraft night.**\nTime when using the `/time set night` command."),
            new DayEvent(13188, "First tick when monsters spawn outdoors in clear weather."),
            new DayEvent(13670, "The internal sky-light level reaches 4, the minimum at night."),
            new DayEvent(13702, "The sun disappears on the horizon."),
            new DayEvent(17843, "The clock starts showing exactly midnight."),
            new DayEvent(18000,
                    "**Midnight**, the moon is at its peak.\nTime when using the `/time set midnight` command."),
            new DayEvent(22300, "The sun appears on the horizon."),
            new DayEvent(22331, "The internal sky-light level begins to increase."),
            new DayEvent(22812, "Last tick when monsters spawn outdoors in clear weather."),
            new DayEvent(23000, "**Beginning of the Minecraft sunrise.**"),
            new DayEvent(23031, "Last tick when monsters spawn outdoors in rainy weather."),
            new DayEvent(23041, "The clock starts showing exactly dawn."),
            new DayEvent(23216, "The solar zenith angle is 0."),
            new DayEvent(23460,
                    "In clear weather, beds can no longer be used, bees leave the nest/hive, and undead mobs begin to burn."),
            new DayEvent(23961,
                    "In sunny weather, the internal sky-light level reaches 15, the maximum. Horizon stops getting brighter."),
            new DayEvent(23992,
                    "In rainy weather, beds can no longer be used and, the internal sky-light level reaches 12, the maximum.")
    };

    public String getDescription() {
        return "Get the current world time of day";
    }

    public String getArgumentsSchema() {
        return """
                {
                    "type": "object",
                    "properties": {
                        "getFutureEventsWithinTicks": {
                            "type": "integer",
                            "description": "If specified, will return a list of upcoming notable events to do with the world's current day cycle within the next specified ticks. If 0 or not provided, no events will be returned, only the current world time. A good value to specify is 1200 ticks, to return notable events within the next minute.",
                            "minimum": 0,
                            "maximum": 24000,
                            "default": 0
                        }
                    }
                }
                """;
    }

    public CallToolResult execute(Object exchange, Map<String, Object> arguments) {
        int timeOfDay = (int) (MC.world.getTimeOfDay() % 24000);

        CallToolResult.Builder builder = new CallToolResult.Builder()
                .isError(false)
                .addTextContent(String.format("""
                        Time of day: %d (ticks)

                        Note that:
                        - 1 tick = 3.6s in Minecraft (50ms in real time)
                        - 1000 ticks = 1 hour in Minecraft (50s in real time)
                        - 24000 ticks = 1 full day in Minecraft (20m in real time)
                        """, timeOfDay));

        int forwardTicks = (int) arguments.getOrDefault("getFutureEventsWithinTicks", 0);

        if (forwardTicks < 0 || forwardTicks > 24000) {
            return new CallToolResult("Invalid value for getFutureEventsWithinTicks. Must be between 0 and 24000.",
                    true);
        }

        if (forwardTicks > 0) {
            StringBuilder upcomingEvents = new StringBuilder();

            for (DayEvent event : DAY_EVENTS) {
                if (event.tick >= timeOfDay && event.tick <= timeOfDay + forwardTicks) {
                    upcomingEvents.append(String.format("""
                            - In %d ticks: %s
                            """, event.tick - timeOfDay, event.info));
                }
            }

            if (upcomingEvents.isEmpty()) {
                upcomingEvents.append("No notable events within the next ")
                        .append(forwardTicks)
                        .append(" ticks.");
            }

            builder.addTextContent(String.format("""
                    Upcoming notable events within the next %d ticks:
                    %s
                    """, forwardTicks, upcomingEvents));
        }

        return builder.build();
    }
}
