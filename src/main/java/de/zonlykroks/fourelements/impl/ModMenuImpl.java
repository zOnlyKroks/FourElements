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
        List<String> availablePresets = config.getAvailablePresets();

        return YetAnotherConfigLib.createBuilder()
                .title(Text.literal("FourElements Configuration"))
                .category(ConfigCategory.createBuilder()
                        .name(Text.literal("General"))
                        .tooltip(Text.literal("General mod settings"))
                        .group(OptionGroup.createBuilder()
                                .name(Text.literal("Preset Configuration"))
                                .description(OptionDescription.of(Text.literal(
                                        "Select which preset to load.\n\n" +
                                        "Available presets in config/fourelements/presets/:\n" +
                                        String.join("\n", availablePresets) + "\n\n" +
                                        "Each preset folder should contain:\n" +
                                        "- rules.json (replacement rules)\n" +
                                        "- textures/ (folder with .png textures)"
                                )))
                                .option(Option.<String>createBuilder()
                                        .name(Text.literal("Active Preset"))
                                        .description(OptionDescription.of(Text.literal(
                                                "Select which preset to use.\n" +
                                                "Type the preset folder name (e.g., example_preset1, example_preset2)"
                                        )))
                                        .binding(
                                                "example_preset1",
                                                config::getSelectedPreset,
                                                config::setSelectedPreset
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

                    TextureReplacementManager.getInstance().reloadWithResourcePack();
                })
                .build()
                .generateScreen(parent);
    }
}
