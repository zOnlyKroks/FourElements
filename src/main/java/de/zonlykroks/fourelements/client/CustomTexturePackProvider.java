package de.zonlykroks.fourelements.client;

import net.minecraft.resource.*;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.function.Consumer;

public class CustomTexturePackProvider implements ResourcePackProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger("FourElements");

    @Override
    public void register(Consumer<ResourcePackProfile> profileAdder) {
        ResourcePackInfo info = new ResourcePackInfo(
                "fourelements_custom_textures",
                Text.literal("FourElements Custom Textures"),
                ResourcePackSource.BUILTIN,
                Optional.empty()
        );

        ResourcePackProfile.PackFactory packFactory = new ResourcePackProfile.PackFactory() {
            @Override
            public ResourcePack open(ResourcePackInfo info) {
                return new CustomTextureResourcePack();
            }

            @Override
            public ResourcePack openWithOverlays(ResourcePackInfo info, ResourcePackProfile.Metadata metadata) {
                return new CustomTextureResourcePack();
            }
        };

        ResourcePackProfile profile = ResourcePackProfile.create(
                info,
                packFactory,
                ResourceType.CLIENT_RESOURCES,
                new ResourcePackPosition(true, ResourcePackProfile.InsertionPosition.TOP, false)
        );

        if (profile != null) {
            profileAdder.accept(profile);
            LOGGER.info("Registered custom texture pack profile");
        }
    }
}
