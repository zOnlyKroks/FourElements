package de.zonlykroks.fourelements.impl;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import de.zonlykroks.fourelements.client.TextureReplacementManager;
import de.zonlykroks.fourelements.config.ModConfig;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.List;

public class ModMenuImpl implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return this::createConfigScreen;
    }

    private Screen createConfigScreen(Screen parent) {
        ModConfig config = ModConfig.getInstance();
        List<String> availableFiles = config.getAvailableRulesFiles();

        return YetAnotherConfigLib.createBuilder()
                .title(Text.literal("FourElements Configuration"))
                .category(ConfigCategory.createBuilder()
                        .name(Text.literal("General"))
                        .tooltip(Text.literal("General mod settings"))
                        .group(OptionGroup.createBuilder()
                                .name(Text.literal("Rules Configuration"))
                                .description(OptionDescription.of(Text.literal(
                                        "Select which JSON rules file to load.\n\n" +
                                        "Available files in config/fourelements/:\n" +
                                        String.join("\n", availableFiles)
                                )))
                                .option(Option.<String>createBuilder()
                                        .name(Text.literal("Rules File"))
                                        .description(OptionDescription.of(Text.literal(
                                                "Select which texture replacement rules file to use.\n" +
                                                "Type the filename (e.g., texture_replacements.json)"
                                        )))
                                        .binding(
                                                "texture_replacements.json",
                                                config::getSelectedRulesFile,
                                                config::setSelectedRulesFile
                                        )
                                        .controller(StringControllerBuilder::create)
                                        .build())
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(Text.literal("Performance"))
                                .description(OptionDescription.of(Text.literal("Performance-related settings")))
                                .option(Option.<Integer>createBuilder()
                                        .name(Text.literal("Cache Size"))
                                        .description(OptionDescription.of(Text.literal("Maximum number of cached sprite replacements (requires restart)")))
                                        .binding(
                                                4096,
                                                config::getCacheSize,
                                                config::setCacheSize
                                        )
                                        .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                                .range(512, 16384)
                                                .step(512))
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(Text.literal("Enable Cache Statistics"))
                                        .description(OptionDescription.of(Text.literal("Log cache hit/miss statistics (may impact performance slightly)")))
                                        .binding(
                                                false,
                                                config::isEnableCacheStats,
                                                config::setEnableCacheStats
                                        )
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(Text.literal("Debugging"))
                                .description(OptionDescription.of(Text.literal("Debug options")))
                                .option(Option.<Boolean>createBuilder()
                                        .name(Text.literal("Enable Debug Logging"))
                                        .description(OptionDescription.of(Text.literal("Enable verbose debug logging")))
                                        .binding(
                                                false,
                                                config::isEnableDebugLogging,
                                                config::setEnableDebugLogging
                                        )
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .build())
                        .build())
                .save(() -> {
                    config.save();

                    TextureReplacementManager.getInstance().reload();
                })
                .build()
                .generateScreen(parent);
    }
}
