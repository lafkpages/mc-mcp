package luisafk.mcmcp.tools.inventory;

import static luisafk.mcmcp.Client.MC;

import java.util.Map;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.Tool;
import luisafk.mcmcp.tools.BaseTool;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;

public class ConsumeItemInHandTool extends BaseTool {
    private int consumeTicks = 0;

    @Override
    public McpServerFeatures.SyncToolSpecification create() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!isPlayerAvailable()) {
                return;
            }

            if (consumeTicks > 0) {
                if (!client.options.useKey.isPressed()) {
                    // For some reason, the key was released before the consumeTicks reached 0, so
                    // we will exit
                    consumeTicks = -1;
                    return;
                }

                consumeTicks--;
            } else if (consumeTicks == 0) {
                client.options.useKey.setPressed(false);

                // Reset consumeTicks to -1 to indicate that the item has been consumed
                consumeTicks = -1;
            }
        });

        return new McpServerFeatures.SyncToolSpecification(
                new Tool("consume_item_in_hand",
                        "Consume the item in the hand of the player. This tool can be used to, for example, eat food items or drink potions. It will not work if the item in hand is not consumable.",
                        EMPTY_ARGUMENTS_SCHEMA),
                this::execute);
    }

    private CallToolResult execute(Object exchange, Map<String, Object> arguments) {
        if (!isPlayerAvailable()) {
            return playerNotFoundError();
        }

        if (MC.options.useKey.isPressed()) {
            return new CallToolResult("Right-click is already pressed", true);
        }

        ItemStack itemStack = MC.player.getMainHandStack();
        ComponentMap components = itemStack.getComponents();

        if (!components.contains(DataComponentTypes.CONSUMABLE)) {
            return new CallToolResult(
                    "The item in hand is not consumable",
                    true);
        }

        consumeTicks = components.get(DataComponentTypes.CONSUMABLE).getConsumeTicks();

        MC.options.useKey.setPressed(true);

        // TODO: Add a delay to allow the item to be consumed

        return new CallToolResult("Started consuming item, will take " + consumeTicks + " ticks", false);
    }
}
