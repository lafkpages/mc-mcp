package luisafk.mcmcp.tools;

import static luisafk.mcmcp.Client.MC;

import java.util.Map;

import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.Content;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;

public abstract class BaseTool {
    private static final long PLAYER_DAMAGE_NOTIFICATION_TIMEOUT = 1200; // 20 seconds

    private long lastPlayerDamageTime;
    private Entity lastPlayerDamageSource;

    public abstract String getName();

    public abstract String getDescription();

    /**
     * Defaults to an empty object schema.
     */
    public String getArgumentsSchema() {
        return """
                {
                    "type": "object",
                    "properties": {},
                    "required": []
                }
                """;
    }

    public void init() {
        ServerLivingEntityEvents.AFTER_DAMAGE.register(new ServerLivingEntityEvents.AfterDamage() {
            @Override
            public void afterDamage(LivingEntity entity, DamageSource source, float baseDamageTaken, float damageTaken,
                    boolean blocked) {

                if (entity.getId() == MC.player.getId()) {
                    lastPlayerDamageTime = MC.world.getTime();
                    lastPlayerDamageSource = source.getSource();
                }
            }
        });
    }

    public abstract CallToolResult execute(Object exchange, Map<String, Object> arguments);

    public CallToolResult handler(Object exchange, Map<String, Object> arguments) {
        if (MC.world == null) {
            return new CallToolResult("World not found - not in game", true);
        }

        if (MC.player == null) {
            return new CallToolResult("Player is null, might not be in-game", true);
        }

        CallToolResult toolResult = execute(exchange, arguments);

        CallToolResult.Builder builder = CallToolResult
                .builder()
                .isError(toolResult.isError());

        for (Content content : toolResult.content()) {
            builder.addContent(content);
        }

        long playerDamageTicksAgo = MC.world.getTime() - lastPlayerDamageTime;
        if (playerDamageTicksAgo < PLAYER_DAMAGE_NOTIFICATION_TIMEOUT) {
            builder.addTextContent(
                    String.format(
                            "Warning: player was recently damaged, %d ticks ago by %s %s",
                            playerDamageTicksAgo,
                            lastPlayerDamageSource.getType(),
                            lastPlayerDamageSource.getName().getString()));
        }

        return builder.build();
    }
}
