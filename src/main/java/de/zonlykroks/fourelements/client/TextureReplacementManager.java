package de.zonlykroks.fourelements.client;

import de.zonlykroks.fourelements.config.TextureReplacementConfig;
import de.zonlykroks.fourelements.config.TextureReplacementRule;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.util.Atlases;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TextureReplacementManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("FourElements");
    private static TextureReplacementManager instance;

    private final TextureReplacementConfig config;

    private TextureReplacementManager() {
        this.config = new TextureReplacementConfig();
    }

    public static TextureReplacementManager getInstance() {
        if (instance == null) {
            instance = new TextureReplacementManager();
        }
        return instance;
    }

    public void initialize() {
        LOGGER.info("Initializing Texture Replacement Manager");
        config.load();
        LOGGER.info("Texture Replacement Manager initialized with {} rules", config.getRules().size());
    }

    public Sprite getReplacementSprite(BlockRenderView world, BlockPos pos, BlockState state, Sprite originalSprite) {
        if (world == null || pos == null || state == null) {
            return null;
        }

        for (TextureReplacementRule rule : config.getRules()) {
            if (rule.matches(world, pos, state)) {
                String textureId = rule.replacementTexture();

                Identifier textureLoc;
                if (textureId.contains(":")) {
                    textureLoc = Identifier.tryParse(textureId);
                } else {
                    if (!textureId.startsWith("block/")) {
                        textureId = "block/" + textureId;
                    }
                    textureLoc = Identifier.of("minecraft", textureId);
                }

                if (textureLoc == null) {
                    LOGGER.warn("Invalid texture identifier: {}", textureId);
                    continue;
                }

                try {
                    SpriteAtlasTexture atlas = MinecraftClient.getInstance().getAtlasManager().getAtlasTexture(Atlases.BLOCKS);
                    Sprite sprite = atlas.getSprite(textureLoc);

                    if (sprite != null && !sprite.getContents().getId().toString().contains("missingno")) {
                        return sprite;
                    } else {
                        LOGGER.warn("Sprite not found in atlas: {} (got missingno)", textureLoc);
                    }
                } catch (Exception e) {
                    LOGGER.error("Failed to get replacement sprite for {}: {}", textureLoc, e.getMessage());
                }
            }
        }

        return null;
    }
}