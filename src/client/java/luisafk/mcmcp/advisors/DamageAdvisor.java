package luisafk.mcmcp.advisors;

import static luisafk.mcmcp.Client.MC;

import java.util.List;

import io.modelcontextprotocol.spec.McpSchema.Content;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;

public class DamageAdvisor extends BaseAdvisor {

    private static final long PLAYER_DAMAGE_NOTIFICATION_TIMEOUT = 1200; // 20 seconds

    private long lastPlayerDamageTime = -1;
    private Entity lastPlayerDamageSource;

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

    public List<Content> get() {
        if (lastPlayerDamageTime < 0) {
            return List.of();
        }

        long playerDamageTicksAgo = MC.world.getTime() - lastPlayerDamageTime;
        if (playerDamageTicksAgo < PLAYER_DAMAGE_NOTIFICATION_TIMEOUT) {
            String source = lastPlayerDamageSource == null ? "unknown source"
                    : String.format(
                            "%s %s",
                            lastPlayerDamageSource.getType(),
                            lastPlayerDamageSource.getName().getString());

            return List.of(new TextContent(
                    String.format(
                            "Warning: player was recently damaged, %d ticks ago by %s",
                            playerDamageTicksAgo,
                            source)));
        }

        return List.of();
    }
}
