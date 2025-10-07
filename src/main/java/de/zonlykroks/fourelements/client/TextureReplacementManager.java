package de.zonlykroks.fourelements.client;

import de.zonlykroks.fourelements.config.ModConfig;
import de.zonlykroks.fourelements.config.TextureReplacementConfig;
import de.zonlykroks.fourelements.config.TextureReplacementRule;
import net.fabricmc.fabric.api.renderer.v1.model.SpriteFinder;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.util.Atlases;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class TextureReplacementManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("FourElements");
    private static TextureReplacementManager instance;

    private final TextureReplacementConfig config;
    private final ModConfig modConfig;
    private SpriteAtlasTexture cachedAtlas;
    private SpriteFinder cachedSpriteFinder;
    private final Map<CacheKey, Sprite> spriteCache = new HashMap<>();
    private int cacheHits = 0;
    private int cacheMisses = 0;

    private TextureReplacementManager() {
        this.config = new TextureReplacementConfig();
        this.modConfig = ModConfig.getInstance();
    }

    public static TextureReplacementManager getInstance() {
        if (instance == null) {
            instance = new TextureReplacementManager();
        }
        return instance;
    }

    public void initialize() {
        LOGGER.info("Initializing Texture Replacement Manager");

        modConfig.load();
        config.load();

        LOGGER.info("Texture Replacement Manager initialized with {} rules from {}",
                config.getRules().size(), modConfig.getSelectedRulesFile());
    }

    public void initializeAtlas() {
        if (cachedAtlas == null) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.getAtlasManager() != null) {
                this.cachedAtlas = client.getAtlasManager().getAtlasTexture(Atlases.BLOCKS);
                this.cachedSpriteFinder = cachedAtlas.spriteFinder();
                LOGGER.info("Atlas manager initialized");
            }
        }
    }

    public void reload() {
        LOGGER.info("Reloading Texture Replacement Manager");
        spriteCache.clear();
        cacheHits = 0;
        cacheMisses = 0;

        config.load();

        LOGGER.info("Reloaded {} texture replacement rules from {}",
                config.getRules().size(), modConfig.getSelectedRulesFile());
    }

    public void clearCacheAndAtlas() {
        LOGGER.info("Clearing cache and reinitializing atlas");
        spriteCache.clear();
        cacheHits = 0;
        cacheMisses = 0;
        cachedAtlas = null;
        cachedSpriteFinder = null;

        initializeAtlas();
    }

    public SpriteFinder getSpriteFinder() {
        return cachedSpriteFinder;
    }

    private record CacheKey(BlockPos pos, BlockState state) {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof CacheKey(BlockPos pos1, BlockState state1))) return false;
            return pos.equals(pos1) && state.equals(state1);
        }

        @Override
        public int hashCode() {
            return 31 * pos.hashCode() + state.hashCode();
        }
    }

    public Sprite getReplacementSprite(BlockRenderView world, BlockPos pos, BlockState state, Sprite originalSprite) {
        if (world == null || pos == null || state == null) {
            return null;
        }

        CacheKey key = new CacheKey(pos.toImmutable(), state);

        if (spriteCache.containsKey(key)) {
            if (modConfig.isEnableCacheStats()) {
                cacheHits++;

                if ((cacheHits + cacheMisses) % 1000 == 0) {
                    logCacheStats();
                }
            }
            return spriteCache.get(key);
        }

        if (modConfig.isEnableCacheStats()) {
            cacheMisses++;
        }

        if (modConfig.isEnableDebugLogging()) {
            LOGGER.debug("Checking replacement for block {} at {} (state: {})",
                state.getBlock().getTranslationKey(), pos, state);
        }

        Sprite result = null;
        boolean hasNeighborConditions = false;

        for (TextureReplacementRule rule : config.getRules()) {
            if (rule.matches(world, pos, state)) {
                Sprite sprite = rule.getReplacementSprite(cachedAtlas);

                if (sprite != null) {
                    if (modConfig.isEnableDebugLogging()) {
                        LOGGER.info("Applied texture replacement: {} -> {} at {}",
                            originalSprite.getContents().getId(),
                            sprite.getContents().getId(),
                            pos);
                    }
                    result = sprite;
                    hasNeighborConditions = rule.hasNeighborConditions();
                    break;
                }
            }
        }

        // Only cache results that don't depend on neighbors (to avoid stale cache on neighbor changes)
        if (!hasNeighborConditions) {
            int cacheSize = modConfig.getCacheSize();
            if (spriteCache.size() < cacheSize) {
                spriteCache.put(key, result);
            }
        }

        return result;
    }

    public void logCacheStats() {
        if (modConfig.isEnableCacheStats()) {
            int total = cacheHits + cacheMisses;
            if (total > 0) {
                double hitRate = (double) cacheHits / total * 100;
                LOGGER.info("Cache stats - Size: {}/{}, Hits: {}, Misses: {}, Hit rate: {}%",
                    spriteCache.size(), modConfig.getCacheSize(), cacheHits, cacheMisses, String.format("%.2f", hitRate));
            }
        }
    }
}