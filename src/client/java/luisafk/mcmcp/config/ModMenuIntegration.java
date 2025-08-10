package luisafk.mcmcp.config;

import static luisafk.mcmcp.Client.CONFIG;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import net.minecraft.text.Text;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parentScreen -> YetAnotherConfigLib.createBuilder()
                .title(Text.literal("MC MCP Config"))
                .category(ConfigCategory.createBuilder()
                        .name(Text.literal("Tools"))
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.literal("Enable advisors"))
                                .binding(true, () -> CONFIG.enableAdvisors, newVal -> CONFIG.enableAdvisors = newVal)
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .build())
                .build()
                .generateScreen(parentScreen);
    }
}