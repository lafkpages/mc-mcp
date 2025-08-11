package luisafk.mcmcp.config;

import static luisafk.mcmcp.Client.MOD_INSTANCE;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import luisafk.mcmcp.tools.ToolRegistry;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class ConfigScreen {

    public static Screen createConfigScreen(Screen parent) {
        return YetAnotherConfigLib.create(Config.HANDLER, (defaults, config, builder) -> {

            OptionGroup.Builder enabledToolsGroup = OptionGroup.createBuilder()
                    .name(Text.literal("Enabled Tools"));

            ToolRegistry.TOOLS.forEach((toolName, tool) -> {
                boolean defaultEnabled = defaults.isToolEnabled(toolName);

                enabledToolsGroup.option(Option.<Boolean>createBuilder()
                        .name(Text.literal(toolName))
                        .binding(defaultEnabled, () -> config.isToolEnabled(toolName),
                                newEnabled -> {
                                    config.enabledTools.put(toolName, newEnabled);

                                    if (newEnabled) {
                                        MOD_INSTANCE.mcpServer.registerTool(
                                                toolName, tool);
                                    } else {
                                        MOD_INSTANCE.mcpServer.unregisterTool(
                                                toolName);
                                    }
                                })
                        .controller(TickBoxControllerBuilder::create)
                        .build());
            });

            return builder
                    .title(Text.literal("MC MCP Config"))
                    .category(ConfigCategory.createBuilder()
                            .name(Text.literal("Tools"))
                            .option(Option.<Boolean>createBuilder()
                                    .name(Text.literal("Enable advisors"))
                                    .binding(defaults.enableAdvisors,
                                            () -> config.enableAdvisors,
                                            newVal -> config.enableAdvisors = newVal)
                                    .controller(TickBoxControllerBuilder::create)
                                    .build())
                            .group(enabledToolsGroup.build())
                            .build());
        })
                .generateScreen(parent);
    }
}