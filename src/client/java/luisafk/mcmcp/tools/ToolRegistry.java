package luisafk.mcmcp.tools;

import java.util.Arrays;
import java.util.List;

import io.modelcontextprotocol.server.McpSyncServer;
import luisafk.mcmcp.tools.baritone.BaritoneGotoTool;
import luisafk.mcmcp.tools.baritone.BaritoneMineTool;
import luisafk.mcmcp.tools.baritone.BaritoneStopTool;
import luisafk.mcmcp.tools.inventory.ConsumeItemInHandTool;
import luisafk.mcmcp.tools.inventory.GetInventoryTool;
import luisafk.mcmcp.tools.inventory.SetSelectedItemTool;
import luisafk.mcmcp.tools.inventory.UseItemInHandOnTargetedBlockTool;
import luisafk.mcmcp.tools.inventory.UseItemInHandTool;
import luisafk.mcmcp.tools.misc.GetPlayerPermissionLevelTool;
import luisafk.mcmcp.tools.misc.RunCommandTool;
import luisafk.mcmcp.tools.player.GetPlayerBiomeTool;
import luisafk.mcmcp.tools.player.GetPlayerDimensionTool;
import luisafk.mcmcp.tools.player.GetPlayerHealthTool;
import luisafk.mcmcp.tools.player.GetPlayerHungerTool;
import luisafk.mcmcp.tools.player.GetPlayerNameTool;
import luisafk.mcmcp.tools.player.GetPlayerPositionTool;
import luisafk.mcmcp.tools.world.GetNearbyEntitiesTool;
import luisafk.mcmcp.tools.world.GetTargetedBlockTool;
import luisafk.mcmcp.tools.world.GetWorldTimeTool;
import luisafk.mcmcp.tools.world.GetWorldWeatherTool;
import luisafk.mcmcp.tools.world.ListOnlinePlayersTool;

public class ToolRegistry {
    private static final List<BaseTool> TOOLS = Arrays.asList(
            // Tools, sorted alphabetically per category

            // Player tools
            new GetPlayerBiomeTool(),
            new GetPlayerDimensionTool(),
            new GetPlayerHealthTool(),
            new GetPlayerHungerTool(),
            new GetPlayerNameTool(),
            new GetPlayerPermissionLevelTool(),
            new GetPlayerPositionTool(),

            // World tools
            new GetNearbyEntitiesTool(),
            new GetTargetedBlockTool(),
            new GetWorldTimeTool(),
            new GetWorldWeatherTool(),
            new ListOnlinePlayersTool(),

            // Inventory tools
            new ConsumeItemInHandTool(),
            new GetInventoryTool(),
            new SetSelectedItemTool(),
            new UseItemInHandOnTargetedBlockTool(),
            new UseItemInHandTool(),

            // Baritone tools
            new BaritoneGotoTool(),
            new BaritoneMineTool(),
            new BaritoneStopTool(),

            // Misc tools
            new RunCommandTool());

    public static void registerAllTools(McpSyncServer mcpServer) {
        for (BaseTool tool : TOOLS) {
            mcpServer.addTool(tool.create());
        }
    }
}
