package luisafk.mcmcp.tools.inventory;

import static luisafk.mcmcp.Client.MC;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import luisafk.mcmcp.tools.BaseTool;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;

public class ConsumeItemInHandTool extends BaseTool {
    private static final int SAFETY_TIMEOUT_MS = 5000;

    private CompletableFuture<CallToolResult> currentConsumption = null;
    private int consumeTicks = 0;

    public String getDescription() {
        return "Consume the item in the hand of the player. This tool can be used to, for example, eat food items or drink potions. It will not work if the item in hand is not consumable.";
    }

    public void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (MC.player == null) {
                return;
            }

            if (consumeTicks > 0) {
                if (!client.options.useKey.isPressed()) {
                    // Key was released before consumption finished
                    consumeTicks = -1;
                    if (currentConsumption != null && !currentConsumption.isDone()) {
                        currentConsumption.cancel(true);
                        currentConsumption = null;
                    }
                    return;
                }

                consumeTicks--;
            } else if (consumeTicks == 0) {
                client.options.useKey.setPressed(false);
                consumeTicks = -1;

                // Complete the future to unblock the tool
                if (currentConsumption != null && !currentConsumption.isDone()) {
                    currentConsumption.complete(new CallToolResult(
                            "Item consumed successfully", false));
                    currentConsumption = null;
                }
            }
        });
    }

    public CallToolResult execute(Object exchange, Map<String, Object> arguments) {
        if (MC.options.useKey.isPressed()) {
            return new CallToolResult("Right-click is already pressed", true);
        }

        ItemStack itemStack = MC.player.getMainHandStack();
        ComponentMap components = itemStack.getComponents();

        if (!components.contains(DataComponentTypes.CONSUMABLE)) {
            return new CallToolResult("The item in hand is not consumable", true);
        }

        // Check if another consumption is in progress
        if (currentConsumption != null && !currentConsumption.isDone()) {
            return new CallToolResult("Another consumption is already in progress", true);
        }

        consumeTicks = components.get(DataComponentTypes.CONSUMABLE).getConsumeTicks();

        // Create a future that will be completed when consumption finishes
        currentConsumption = new CompletableFuture<>();

        // Start consuming
        MC.options.useKey.setPressed(true);

        try {
            // Wait for consumption to complete (with timeout)
            // Add extra time for safety
            return currentConsumption.get(consumeTicks * 50 + SAFETY_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (java.util.concurrent.TimeoutException e) {
            MC.options.useKey.setPressed(false);
            consumeTicks = -1;
            currentConsumption = null;
            return new CallToolResult("Consumption timed out: " + e.getMessage(), true);
        } catch (java.util.concurrent.ExecutionException e) {
            MC.options.useKey.setPressed(false);
            consumeTicks = -1;
            currentConsumption = null;
            return new CallToolResult("Error during consumption execution: " + e.getCause(), true);
        } catch (InterruptedException e) {
            MC.options.useKey.setPressed(false);
            consumeTicks = -1;
            currentConsumption = null;
            Thread.currentThread().interrupt();
            return new CallToolResult("Consumption was interrupted: " + e.getMessage(), true);
        }
    }
}
