package de.zonlykroks.fourelements.client;

import de.zonlykroks.fourelements.config.ModConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FourelementsClient implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger("FourElements");

    private static KeyBinding cyclePresetKey;

    @Override
    public void onInitializeClient() {
        LOGGER.info("Initializing FourElements client mod");

        TextureReplacementManager.getInstance().initialize();

        // Register keybinding
        cyclePresetKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.fourelements.cycle_preset",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_P,
                new KeyBinding.Category(Identifier.of("fourelements", "category.fourelements"))
        ));

        // Register commands
        ClientCommandRegistrationCallback.EVENT.register(PresetCycleCommand::register);

        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            client.getResourcePackManager().providers.add(new CustomTexturePackProvider());
            LOGGER.info("Registered custom texture pack provider - textures from config/fourelements/textures will be loaded");
        });

        // Register resource reload listener to clear cache when resources change
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public Identifier getFabricId() {
                return Identifier.of("fourelements", "resource_reload");
            }

            @Override
            public void reload(ResourceManager manager) {
                LOGGER.info("Resources reloaded, clearing texture replacement cache and reinitializing atlas");
                TextureReplacementManager textureManager = TextureReplacementManager.getInstance();
                textureManager.clearCacheAndAtlas();
            }
        });

        ModelLoadingPlugin.register(new PositionAwareModelLoadingPlugin());

        // Handle keybinding press
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.END_CLIENT_TICK.register(c -> {
                while (cyclePresetKey.wasPressed()) {
                    String newPreset = ModConfig.getInstance().cyclePreset();
                    TextureReplacementManager.getInstance().reloadWithResourcePack();

                    if (c.player != null) {
                        c.player.sendMessage(
                                Text.literal("§aSwitched to preset: §e" + newPreset + "§a. Reloading resources..."),
                                true
                        );
                    }
                    LOGGER.info("Cycled to preset: {}", newPreset);
                }
            });
        });

        // Log cache stats periodically when enabled
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            LOGGER.info("Client stopping, logging final cache statistics");
            TextureReplacementManager.getInstance().logCacheStats();
        });

        LOGGER.info("FourElements client initialization complete");
    }
}