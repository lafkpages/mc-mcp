package luisafk.mcmcp.tools.player;

import static luisafk.mcmcp.Client.MC;

import java.util.Map;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.Tool;
import luisafk.mcmcp.tools.BaseTool;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class AttackTargetedBlockTool extends BaseTool {
    private int attackTicks = 0;
    private BlockPos targetBlockPos = null;
    private Direction targetSide = null;

    @Override
    public McpServerFeatures.SyncToolSpecification create() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!isPlayerAvailable()) {
                return;
            }

            if (attackTicks > 0) {
                if (!client.options.attackKey.isPressed()) {
                    // For some reason, the key was released before the attackTicks reached 0, so
                    // we will exit
                    attackTicks = -1;
                    targetBlockPos = null;
                    targetSide = null;
                    return;
                }

                // Continue attacking the same block
                if (targetBlockPos != null && targetSide != null) {
                    MC.interactionManager.attackBlock(targetBlockPos, targetSide);
                }

                attackTicks--;
            } else if (attackTicks == 0) {
                client.options.attackKey.setPressed(false);

                // Reset attackTicks to -1 to indicate that the block attack has finished
                attackTicks = -1;
                targetBlockPos = null;
                targetSide = null;
            }
        });

        return new McpServerFeatures.SyncToolSpecification(
                new Tool("attack_targeted_block",
                        "Attack (mine/break) the block the player is currently looking at.",
                        EMPTY_ARGUMENTS_SCHEMA),
                this::execute);
    }

    private CallToolResult execute(Object exchange, Map<String, Object> arguments) {
        if (!isPlayerAvailable()) {
            return playerNotFoundError();
        }

        if (MC.options.attackKey.isPressed()) {
            return new CallToolResult("Attack key is already pressed", true);
        }

        if (MC.crosshairTarget.getType() != HitResult.Type.BLOCK) {
            return new CallToolResult(
                    String.format("Not targeting a block (got a hit type of %s)", MC.crosshairTarget.getType()),
                    true);
        }

        BlockHitResult blockHit = (BlockHitResult) MC.crosshairTarget;
        BlockPos blockPos = blockHit.getBlockPos();
        Direction side = blockHit.getSide();

        // Calculate how long it takes to break this block
        float breakTime = MC.world.getBlockState(blockPos).calcBlockBreakingDelta(MC.player, MC.world, blockPos);
        if (breakTime <= 0) {
            return new CallToolResult("This block cannot be broken", true);
        }

        // Convert break time to ticks (breakTime is progress per tick, so 1/breakTime
        // gives total ticks)
        int ticksToBreak = (int) Math.ceil(1.0f / breakTime);

        // Store the target for continuous attacking
        this.targetBlockPos = blockPos;
        this.targetSide = side;
        this.attackTicks = ticksToBreak;

        // Start attacking the block
        boolean success = MC.interactionManager.attackBlock(blockPos, side);
        if (!success) {
            // Reset if initial attack failed
            this.attackTicks = -1;
            this.targetBlockPos = null;
            this.targetSide = null;
            return new CallToolResult("Failed to attack the targeted block", true);
        }

        MC.options.attackKey.setPressed(true);

        return new CallToolResult(
                String.format("Started attacking block at %s from side %s, will take %d ticks to break",
                        blockPos.toShortString(), side.asString(), ticksToBreak),
                false);
    }
}
