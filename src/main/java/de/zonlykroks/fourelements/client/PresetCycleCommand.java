package de.zonlykroks.fourelements.client;

import com.mojang.brigadier.CommandDispatcher;
import de.zonlykroks.fourelements.config.ModConfig;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.Text;

public class PresetCycleCommand {
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        dispatcher.register(ClientCommandManager.literal("fourelements")
                .then(ClientCommandManager.literal("cycle")
                        .executes(context -> {
                            String newPreset = ModConfig.getInstance().cyclePreset();
                            TextureReplacementManager.getInstance().reloadWithResourcePack();

                            context.getSource().sendFeedback(
                                    Text.literal("§aSwitched to preset: §e" + newPreset + "§a. Reloading resources...")
                            );

                            return 1;
                        }))
                .then(ClientCommandManager.literal("reload")
                        .executes(context -> {
                            String currentPreset = ModConfig.getInstance().getSelectedPreset();
                            TextureReplacementManager.getInstance().reloadWithResourcePack();

                            context.getSource().sendFeedback(
                                    Text.literal("§aReloading preset: §e" + currentPreset + "§a...")
                            );

                            return 1;
                        }))
                .then(ClientCommandManager.literal("list")
                        .executes(context -> {
                            ModConfig config = ModConfig.getInstance();
                            String current = config.getSelectedPreset();
                            var presets = config.getAvailablePresets();

                            context.getSource().sendFeedback(
                                    Text.literal("§6Available presets:")
                            );

                            for (String preset : presets) {
                                String marker = preset.equals(current) ? " §a(active)" : "";
                                context.getSource().sendFeedback(
                                        Text.literal("  §7- §e" + preset + marker)
                                );
                            }

                            return 1;
                        }))
        );
    }
}
