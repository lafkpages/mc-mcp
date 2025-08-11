package luisafk.mcmcp.tools;

import java.util.HashMap;
import java.util.Map;

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
import luisafk.mcmcp.tools.player.AttackTargetedBlockTool;
import luisafk.mcmcp.tools.player.CraftItemTool;
import luisafk.mcmcp.tools.player.GetPlayerBiomeTool;
import luisafk.mcmcp.tools.player.GetPlayerDimensionTool;
import luisafk.mcmcp.tools.player.GetPlayerHealthTool;
import luisafk.mcmcp.tools.player.GetPlayerHungerTool;
import luisafk.mcmcp.tools.player.GetPlayerNameTool;
import luisafk.mcmcp.tools.player.GetPlayerPositionTool;
import luisafk.mcmcp.tools.player.LookAtPositionTool;
import luisafk.mcmcp.tools.world.GetNearbyBlocksTool;
import luisafk.mcmcp.tools.world.GetNearbyEntitiesTool;
import luisafk.mcmcp.tools.world.GetTargetedBlockTool;
import luisafk.mcmcp.tools.world.GetWorldTimeTool;
import luisafk.mcmcp.tools.world.GetWorldWeatherTool;
import luisafk.mcmcp.tools.world.ListOnlinePlayersTool;

public class ToolRegistry {
    public static final Map<String, BaseTool> TOOLS = new HashMap<>();
    static {
        // Tools, sorted alphabetically per category

        // Player tools
        TOOLS.put("attack_targeted_block", new AttackTargetedBlockTool());
        TOOLS.put("craft_item", new CraftItemTool());
        TOOLS.put("get_player_biome", new GetPlayerBiomeTool());
        TOOLS.put("get_player_dimension", new GetPlayerDimensionTool());
        TOOLS.put("get_player_health", new GetPlayerHealthTool());
        TOOLS.put("get_player_hunger", new GetPlayerHungerTool());
        TOOLS.put("get_player_name", new GetPlayerNameTool());
        TOOLS.put("get_player_permission_level", new GetPlayerPermissionLevelTool());
        TOOLS.put("get_player_position", new GetPlayerPositionTool());
        TOOLS.put("look_at_position", new LookAtPositionTool());

        // World tools
        TOOLS.put("get_nearby_blocks", new GetNearbyBlocksTool());
        TOOLS.put("get_nearby_entities", new GetNearbyEntitiesTool());
        TOOLS.put("get_targeted_block", new GetTargetedBlockTool());
        TOOLS.put("get_world_time", new GetWorldTimeTool());
        TOOLS.put("get_world_weather", new GetWorldWeatherTool());
        TOOLS.put("list_online_players", new ListOnlinePlayersTool());

        // Inventory tools
        TOOLS.put("consume_item_in_hand", new ConsumeItemInHandTool());
        TOOLS.put("get_inventory", new GetInventoryTool());
        TOOLS.put("set_selected_item", new SetSelectedItemTool());
        TOOLS.put("use_item_in_hand_on_targeted_block", new UseItemInHandOnTargetedBlockTool());
        TOOLS.put("use_item_in_hand", new UseItemInHandTool());

        // Baritone tools
        TOOLS.put("baritone_goto", new BaritoneGotoTool());
        TOOLS.put("baritone_mine", new BaritoneMineTool());
        TOOLS.put("baritone_stop", new BaritoneStopTool());

        // Misc tools
        TOOLS.put("run_command", new RunCommandTool());
    }
}
