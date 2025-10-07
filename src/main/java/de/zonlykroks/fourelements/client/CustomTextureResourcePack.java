package de.zonlykroks.fourelements.client;

import de.zonlykroks.fourelements.config.ModConfig;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resource.*;
import net.minecraft.resource.metadata.PackResourceMetadata;
import net.minecraft.resource.metadata.ResourceMetadataSerializer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Range;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

public class CustomTextureResourcePack implements ResourcePack {
    private static final String NAMESPACE = "fourelements";
    private final ResourcePackInfo info;

    public CustomTextureResourcePack() {
        this.info = new ResourcePackInfo(
                "fourelements_custom_textures",
                Text.literal("FourElements Custom Textures"),
                null,
                Optional.empty()
        );
    }

    @Override
    public @Nullable InputSupplier<InputStream> openRoot(String... segments) {
        return null;
    }

    @Override
    public @Nullable InputSupplier<InputStream> open(ResourceType type, Identifier id) {
        if (type != ResourceType.CLIENT_RESOURCES || !id.getNamespace().equals(NAMESPACE)) {
            return null;
        }

        String path = id.getPath();

        if (path.startsWith("textures/")) {
            String texturePath = path.substring("textures/".length());

            // Handle preset-relative textures (e.g., "preset/my_texture")
            if (texturePath.startsWith("preset/")) {
                String presetTexturePath = texturePath.substring("preset/".length());
                if (!presetTexturePath.endsWith(".png")) {
                    presetTexturePath += ".png";
                }

                Path textureFile = ModConfig.getInstance().getPresetTexturesDir().resolve(presetTexturePath);
                if (Files.exists(textureFile) && Files.isRegularFile(textureFile)) {
                    return () -> Files.newInputStream(textureFile);
                }
            } else if (texturePath.startsWith("block/")) {
                // Legacy support: check in current preset's textures dir
                texturePath = texturePath.substring("block/".length());
                Path textureFile = ModConfig.getInstance().getPresetTexturesDir().resolve(texturePath);
                if (Files.exists(textureFile) && Files.isRegularFile(textureFile)) {
                    return () -> Files.newInputStream(textureFile);
                }
            }
        }

        return null;
    }

    @Override
    public void findResources(ResourceType type, String namespace, String prefix, ResultConsumer consumer) {
        if (type != ResourceType.CLIENT_RESOURCES || !namespace.equals(NAMESPACE)) {
            return;
        }

        if (!prefix.startsWith("textures/")) {
            return;
        }

        try {
            Path texturesDir = ModConfig.getInstance().getPresetTexturesDir();
            if (Files.exists(texturesDir) && Files.isDirectory(texturesDir)) {
                Files.walk(texturesDir)
                        .filter(Files::isRegularFile)
                        .filter(path -> path.toString().endsWith(".png"))
                        .forEach(path -> {
                            try {
                                String relativePath = texturesDir.relativize(path).toString().replace('\\', '/');
                                // Register as preset-relative paths
                                Identifier id = Identifier.of(NAMESPACE, "textures/preset/" + relativePath);
                                consumer.accept(id, () -> Files.newInputStream(path));
                            } catch (Exception ignored) {}
                        });
            }
        } catch (Exception ignored) {}
    }

    @Override
    public Set<String> getNamespaces(ResourceType type) {
        return type == ResourceType.CLIENT_RESOURCES ? Set.of(NAMESPACE) : Set.of();
    }

    @Override
    public @Nullable <T> T parseMetadata(ResourceMetadataSerializer<T> metadataSerializer){
        if (metadataSerializer == PackResourceMetadata.DESCRIPTION_SERIALIZER) {
            PackResourceMetadata metadata = new PackResourceMetadata(
                    Text.literal("FourElements custom block textures"),
                    new Range<>(new PackVersion(0,0), new PackVersion(999,999))
            );
            return (T) metadata;
        }
        return null;
    }

    @Override
    public ResourcePackInfo getInfo() {
        return info;
    }

    @Override
    public void close() {}
}
