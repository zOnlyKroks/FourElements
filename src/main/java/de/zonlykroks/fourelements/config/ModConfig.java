package de.zonlykroks.fourelements.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class ModConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("FourElements");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir().resolve("fourelements");
    private static final Path PRESETS_DIR = CONFIG_DIR.resolve("presets");
    private static final Path MOD_CONFIG_FILE = CONFIG_DIR.resolve("config.json");

    private static ModConfig instance;

    private String selectedPreset = "example_preset1";
    private int cacheSize = 4096;
    private boolean enableCacheStats = false;
    private boolean enableDebugLogging = false;

    private ModConfig() {
    }

    public static ModConfig getInstance() {
        if (instance == null) {
            instance = new ModConfig();
        }
        return instance;
    }

    public void load() {
        try {
            if (!Files.exists(CONFIG_DIR)) {
                Files.createDirectories(CONFIG_DIR);
                LOGGER.info("Created FourElements config directory at: {}", CONFIG_DIR);
            }

            if (!Files.exists(PRESETS_DIR)) {
                Files.createDirectories(PRESETS_DIR);
                LOGGER.info("Created presets directory at: {}", PRESETS_DIR);
                createDefaultPresets();
            }

            if (!Files.exists(MOD_CONFIG_FILE)) {
                save();
                LOGGER.info("Created default mod config at: {}", MOD_CONFIG_FILE);
            } else {
                String json = Files.readString(MOD_CONFIG_FILE);
                JsonObject root = GSON.fromJson(json, JsonObject.class);

                // Support legacy config
                if (root.has("selectedRulesFile")) {
                    selectedPreset = root.get("selectedRulesFile").getAsString().replace(".json", "");
                }
                if (root.has("selectedPreset")) {
                    selectedPreset = root.get("selectedPreset").getAsString();
                }
                if (root.has("cacheSize")) {
                    cacheSize = root.get("cacheSize").getAsInt();
                }
                if (root.has("enableCacheStats")) {
                    enableCacheStats = root.get("enableCacheStats").getAsBoolean();
                }
                if (root.has("enableDebugLogging")) {
                    enableDebugLogging = root.get("enableDebugLogging").getAsBoolean();
                }

                LOGGER.info("Loaded mod config - Preset: {}, Cache size: {}", selectedPreset, cacheSize);
            }
        } catch (IOException e) {
            LOGGER.error("Failed to load mod config", e);
        }
    }

    public void save() {
        try {
            JsonObject root = new JsonObject();
            root.addProperty("selectedPreset", selectedPreset);
            root.addProperty("cacheSize", cacheSize);
            root.addProperty("enableCacheStats", enableCacheStats);
            root.addProperty("enableDebugLogging", enableDebugLogging);

            Files.writeString(MOD_CONFIG_FILE, GSON.toJson(root));
            LOGGER.info("Saved mod config");
        } catch (IOException e) {
            LOGGER.error("Failed to save mod config", e);
        }
    }

    public List<String> getAvailablePresets() {
        List<String> presets = new ArrayList<>();
        try (Stream<Path> paths = Files.list(PRESETS_DIR)) {
            paths.filter(Files::isDirectory)
                    .forEach(path -> presets.add(path.getFileName().toString()));
        } catch (IOException e) {
            LOGGER.error("Failed to list available presets", e);
        }

        if (presets.isEmpty()) {
            presets.add("example_preset1");
            presets.add("example_preset2");
        }

        presets.sort(String::compareTo);
        return presets;
    }

    public Path getPresetDir() {
        return PRESETS_DIR.resolve(selectedPreset);
    }

    public Path getPresetRulesFile() {
        return getPresetDir().resolve("rules.json");
    }

    public Path getPresetTexturesDir() {
        return getPresetDir().resolve("textures");
    }

    public String getSelectedPreset() {
        return selectedPreset;
    }

    public void setSelectedPreset(String selectedPreset) {
        this.selectedPreset = selectedPreset;
        save();
    }

    public String cyclePreset() {
        List<String> presets = getAvailablePresets();
        if (presets.isEmpty()) {
            return selectedPreset;
        }

        int currentIndex = presets.indexOf(selectedPreset);
        int nextIndex = (currentIndex + 1) % presets.size();
        String nextPreset = presets.get(nextIndex);

        setSelectedPreset(nextPreset);
        LOGGER.info("Cycled to preset: {}", nextPreset);

        return nextPreset;
    }

    public static Path getPresetsDir() {
        return PRESETS_DIR;
    }

    public int getCacheSize() {
        return cacheSize;
    }

    public void setCacheSize(int cacheSize) {
        this.cacheSize = cacheSize;
    }

    public boolean isEnableCacheStats() {
        return enableCacheStats;
    }

    public void setEnableCacheStats(boolean enableCacheStats) {
        this.enableCacheStats = enableCacheStats;
    }

    public boolean isEnableDebugLogging() {
        return enableDebugLogging;
    }

    public void setEnableDebugLogging(boolean enableDebugLogging) {
        this.enableDebugLogging = enableDebugLogging;
    }

    private void createDefaultPresets() {
        try {
            // Create "example_preset1" preset
            Path preset1 = PRESETS_DIR.resolve("example_preset1");
            createPresetStructure(preset1, getExamplePreset1Rules());
            LOGGER.info("Created default preset: example_preset1");

            // Create "example_preset2" preset
            Path preset2 = PRESETS_DIR.resolve("example_preset2");
            createPresetStructure(preset2, getExamplePreset2Rules());
            LOGGER.info("Created default preset: example_preset2");

        } catch (IOException e) {
            LOGGER.error("Failed to create default presets", e);
        }
    }

    private void createPresetStructure(Path presetDir, String rulesJson) throws IOException {
        if (!Files.exists(presetDir)) {
            Files.createDirectories(presetDir);
        }

        Path rulesFile = presetDir.resolve("rules.json");
        if (!Files.exists(rulesFile)) {
            Files.writeString(rulesFile, rulesJson);
        }

        Path texturesDir = presetDir.resolve("textures");
        if (!Files.exists(texturesDir)) {
            Files.createDirectories(texturesDir);
        }

        // Create a README in the textures folder
        Path readmeFile = texturesDir.resolve("README.txt");
        if (!Files.exists(readmeFile)) {
            Files.writeString(readmeFile,
                "Place your custom .png texture files here.\n\n" +
                "Reference them in rules.json using:\n" +
                "  \"replacementTexture\": \"your_texture.png\"\n\n" +
                "Or use Minecraft textures:\n" +
                "  \"replacementTexture\": \"minecraft:block/diamond_block\""
            );
        }
    }

    private String getExamplePreset1Rules() {
        return """
        {
          "rules": [
            {
              "targetBlocks": ["stone"],
              "positionConditions": [
                {
                  "axis": "y",
                  "operator": "%",
                  "value": 0,
                  "modulo": 2
                }
              ],
              "replacementTexture": "minecraft:block/diamond_block"
            },
            {
              "targetBlocks": ["dirt"],
              "neighborConditions": [
                {
                  "direction": "UP",
                  "targetBlock": "water"
                }
              ],
              "replacementTexture": "minecraft:block/coarse_dirt"
            },
            {
              "targetBlocks": ["cobblestone"],
              "positionConditions": [
                {
                  "axis": "x",
                  "operator": "%",
                  "value": 1,
                  "modulo": 2
                }
              ],
              "replacementTexture": "minecraft:block/mossy_cobblestone"
            }
          ]
        }
        """;
    }

    private String getExamplePreset2Rules() {
        return """
        {
          "rules": [
            {
              "targetBlocks": ["oak_planks"],
              "replacementTexture": "minecraft:block/spruce_planks"
            },
            {
              "targetBlocks": ["stone"],
              "positionConditions": [
                {
                  "axis": "y",
                  "operator": ">",
                  "value": 80
                }
              ],
              "replacementTexture": "minecraft:block/calcite"
            },
            {
              "targetBlocks": ["grass_block"],
              "positionConditions": [
                {
                  "axis": "x",
                  "operator": "%",
                  "value": 0,
                  "modulo": 3
                }
              ],
              "replacementTexture": "minecraft:block/moss_block"
            }
          ]
        }
        """;
    }
}