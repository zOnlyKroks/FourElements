package de.zonlykroks.fourelements.client;

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
    private static final Path TEXTURES_DIR = FabricLoader.getInstance().getConfigDir().resolve("fourelements/textures");
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

            if (texturePath.startsWith("block/")) {
                texturePath = texturePath.substring("block/".length());
            }

            Path textureFile = TEXTURES_DIR.resolve(texturePath);
            if (Files.exists(textureFile) && Files.isRegularFile(textureFile)) {
                return () -> Files.newInputStream(textureFile);
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
            if (Files.exists(TEXTURES_DIR) && Files.isDirectory(TEXTURES_DIR)) {
                Files.walk(TEXTURES_DIR)
                        .filter(Files::isRegularFile)
                        .filter(path -> path.toString().endsWith(".png"))
                        .forEach(path -> {
                            try {
                                String relativePath = TEXTURES_DIR.relativize(path).toString().replace('\\', '/');
                                Identifier id = Identifier.of(NAMESPACE, "textures/block/" + relativePath);
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
