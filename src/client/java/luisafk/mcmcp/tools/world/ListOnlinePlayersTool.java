package luisafk.mcmcp.tools.world;

import static luisafk.mcmcp.Client.MC;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import luisafk.mcmcp.tools.BaseTool;
import net.minecraft.client.network.AbstractClientPlayerEntity;

public class ListOnlinePlayersTool extends BaseTool {

    public String getDescription() {
        return "Get the list of online players (excluding the current player)";
    }

    public CallToolResult execute(Object exchange, Map<String, Object> arguments) {
        List<AbstractClientPlayerEntity> players = MC.world.getPlayers();
        UUID currentPlayerUuid = MC.player.getUuid();

        StringBuilder playersListBuilder = new StringBuilder();

        for (AbstractClientPlayerEntity player : players) {
            if (!player.getUuid().equals(currentPlayerUuid)) {
                if (playersListBuilder.length() > 0) {
                    playersListBuilder.append(", ");
                }

                playersListBuilder.append(player.getName().getString());
            }
        }

        String playersList = playersListBuilder.length() == 0
                ? "No other players online"
                : playersListBuilder.toString();

        return new CallToolResult(
                String.format("Other online players (%d excluding you): %s",
                        players.size() - 1,
                        playersList),
                false);
    }
}
