package de.zonlykroks.fourelements.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
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

        ModelLoadingPlugin.register(new PositionAwareModelLoadingPlugin());

        LOGGER.info("FourElements client initialization complete");
    }
}