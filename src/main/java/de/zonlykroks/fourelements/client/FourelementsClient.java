package de.zonlykroks.fourelements.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FourelementsClient implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger("FourElements");

    @Override
    public void onInitializeClient() {
        LOGGER.info("Initializing FourElements client mod");

        TextureReplacementManager.getInstance().initialize();

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

        // Log cache stats periodically when enabled
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            LOGGER.info("Client stopping, logging final cache statistics");
            TextureReplacementManager.getInstance().logCacheStats();
        });

        LOGGER.info("FourElements client initialization complete");
    }
}