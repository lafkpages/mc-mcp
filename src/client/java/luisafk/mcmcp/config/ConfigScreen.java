package luisafk.mcmcp.config;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class ConfigScreen {

    public static Screen createConfigScreen(Screen parent) {
        return YetAnotherConfigLib.create(Config.HANDLER, (defaults, config, builder) -> builder
                .title(Text.literal("MC MCP Config"))
                .category(ConfigCategory.createBuilder()
                        .name(Text.literal("Tools"))
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.literal("Enable advisors"))
                                .binding(true, () -> config.enableAdvisors,
                                        newVal -> config.enableAdvisors = newVal)
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .build()))
                .generateScreen(parent);
    }
}