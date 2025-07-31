package luisafk.mcmcp.tools.player;

import static luisafk.mcmcp.Client.MC;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

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
    private static final int SAFETY_TIMEOUT_MS = 5000;

    private CompletableFuture<CallToolResult> currentMining = null;
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
                    // Key was released before mining finished
                    attackTicks = -1;
                    targetBlockPos = null;
                    targetSide = null;
                    if (currentMining != null && !currentMining.isDone()) {
                        currentMining.complete(new CallToolResult(
                                "Mining cancelled - attack key was released", true));
                        currentMining = null;
                    }
                    return;
                }

                // Continue attacking the same block
                if (targetBlockPos != null && targetSide != null) {
                    // Check if block still exists
                    if (MC.world.getBlockState(targetBlockPos).isAir()) {
                        // Block was broken!
                        client.options.attackKey.setPressed(false);
                        attackTicks = -1;
                        targetBlockPos = null;
                        targetSide = null;
                        if (currentMining != null && !currentMining.isDone()) {
                            currentMining.complete(new CallToolResult(
                                    "Block broken successfully", false));
                            currentMining = null;
                        }
                        return;
                    }

                    MC.interactionManager.attackBlock(targetBlockPos, targetSide);
                }

                attackTicks--;
            } else if (attackTicks == 0) {
                client.options.attackKey.setPressed(false);
                attackTicks = -1;

                // Check final state
                boolean blockBroken = targetBlockPos != null &&
                        MC.world.getBlockState(targetBlockPos).isAir();

                targetBlockPos = null;
                targetSide = null;

                if (currentMining != null && !currentMining.isDone()) {
                    currentMining.complete(new CallToolResult(
                            blockBroken ? "Block broken successfully"
                                    : "Mining completed but block not broken (may be unbreakable)",
                            !blockBroken));
                    currentMining = null;
                }
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

        // Check if another mining operation is in progress
        if (currentMining != null && !currentMining.isDone()) {
            return new CallToolResult("Another mining operation is already in progress", true);
        }

        BlockHitResult blockHit = (BlockHitResult) MC.crosshairTarget;
        BlockPos blockPos = blockHit.getBlockPos();

        // Calculate how long it takes to break this block
        float damage = MC.world.getBlockState(blockPos).calcBlockBreakingDelta(MC.player, MC.world, blockPos);

        if (damage > 1) {
            // Can be broken instantly
            boolean success = MC.interactionManager.breakBlock(blockPos);

            if (!success) {
                return new CallToolResult("Failed to instantly break the targeted block", true);
            }

            return new CallToolResult("Block broken instantly", false);
        }

        // Convert break time to ticks
        int ticksToBreak = (int) Math.ceil(1.0f / damage);

        // For damage and ticksToBreak calculations see
        // https://minecraft.fandom.com/wiki/Breaking#Calculation

        // Store the target for continuous attacking
        targetBlockPos = blockPos;
        targetSide = blockHit.getSide();
        attackTicks = ticksToBreak;

        // Create a future that will be completed when mining finishes
        currentMining = new CompletableFuture<>();

        // Start attacking the block
        boolean success = MC.interactionManager.attackBlock(blockPos, targetSide);
        if (!success) {
            // Reset if initial attack failed
            attackTicks = -1;
            targetBlockPos = null;
            targetSide = null;
            currentMining = null;
            return new CallToolResult("Failed to attack the targeted block", true);
        }

        MC.options.attackKey.setPressed(true);

        try {
            // Wait for mining to complete (with timeout)
            // Add extra time for safety
            return currentMining.get(ticksToBreak * 50 + SAFETY_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (java.util.concurrent.TimeoutException e) {
            MC.options.attackKey.setPressed(false);
            attackTicks = -1;
            targetBlockPos = null;
            targetSide = null;
            currentMining = null;
            return new CallToolResult("Mining timed out: " + e.getMessage(), true);
        } catch (java.util.concurrent.ExecutionException e) {
            MC.options.attackKey.setPressed(false);
            attackTicks = -1;
            targetBlockPos = null;
            targetSide = null;
            currentMining = null;
            return new CallToolResult("Execution error during mining: " + e.getCause(), true);
        } catch (InterruptedException e) {
            MC.options.attackKey.setPressed(false);
            attackTicks = -1;
            targetBlockPos = null;
            targetSide = null;
            currentMining = null;
            Thread.currentThread().interrupt();
            return new CallToolResult("Mining was interrupted: " + e.getMessage(), true);
        }
    }
}
