package de.zonlykroks.fourelements.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TextureReplacementConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("FourElements");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir().resolve("fourelements");

    private volatile List<TextureReplacementRule> rules = new ArrayList<>();

    public void load() {
        load(ModConfig.getInstance().getPresetRulesFile());
    }

    public void load(Path configFile) {
        try {
            Path presetDir = configFile.getParent();
            if (!Files.exists(presetDir)) {
                Files.createDirectories(presetDir);
                LOGGER.info("Created preset directory at: {}", presetDir);
            }

            Path texturesDir = presetDir.resolve("textures");
            if (!Files.exists(texturesDir)) {
                Files.createDirectories(texturesDir);
                LOGGER.info("Created textures directory at: {}", texturesDir);
            }

            if (!Files.exists(configFile)) {
                createDefaultConfig(configFile);
                LOGGER.info("Created default texture replacement config at: {}", configFile);
            }

            String json = Files.readString(configFile);
            JsonObject root = GSON.fromJson(json, JsonObject.class);
            JsonArray rulesArray = root.getAsJsonArray("rules");

            // Build new list atomically to avoid concurrent modification
            List<TextureReplacementRule> newRules = new ArrayList<>();
            for (int i = 0; i < rulesArray.size(); i++) {
                JsonObject ruleJson = rulesArray.get(i).getAsJsonObject();
                TextureReplacementRule rule = parseRule(ruleJson);
                newRules.add(rule);
            }

            // Atomically replace the rules list
            this.rules = Collections.unmodifiableList(newRules);

            LOGGER.info("Loaded {} texture replacement rules from {}", rules.size(), configFile.getFileName());
        } catch (IOException e) {
            LOGGER.error("Failed to load texture replacement config from {}", configFile, e);
        }
    }

    private TextureReplacementRule parseRule(JsonObject json) {
        List<String> targetBlocks = new ArrayList<>();
        if (json.has("targetBlocks")) {
            JsonArray blocksArray = json.getAsJsonArray("targetBlocks");
            for (int i = 0; i < blocksArray.size(); i++) {
                targetBlocks.add(blocksArray.get(i).getAsString());
            }
        }

        List<TextureReplacementRule.PositionCondition> positionConditions = new ArrayList<>();
        if (json.has("positionConditions")) {
            JsonArray conditionsArray = json.getAsJsonArray("positionConditions");
            for (int i = 0; i < conditionsArray.size(); i++) {
                positionConditions.add(TextureReplacementRule.PositionCondition.fromJson(
                        conditionsArray.get(i).getAsJsonObject()));
            }
        }

        List<TextureReplacementRule.NeighborCondition> neighborConditions = new ArrayList<>();
        if (json.has("neighborConditions")) {
            JsonArray conditionsArray = json.getAsJsonArray("neighborConditions");
            for (int i = 0; i < conditionsArray.size(); i++) {
                neighborConditions.add(TextureReplacementRule.NeighborCondition.fromJson(
                        conditionsArray.get(i).getAsJsonObject()));
            }
        }

        List<TextureReplacementRule.BlockStateCondition> blockStateConditions = new ArrayList<>();
        if (json.has("blockStateConditions")) {
            JsonArray conditionsArray = json.getAsJsonArray("blockStateConditions");
            for (int i = 0; i < conditionsArray.size(); i++) {
                blockStateConditions.add(TextureReplacementRule.BlockStateCondition.fromJson(
                        conditionsArray.get(i).getAsJsonObject()));
            }
        }

        String replacementTexture = json.get("replacementTexture").getAsString();

        return new TextureReplacementRule(targetBlocks, positionConditions, neighborConditions, blockStateConditions, replacementTexture);
    }

    private void createDefaultConfig(Path configFile) throws IOException {
        // Empty default - presets are created by ModConfig
        JsonObject root = new JsonObject();
        JsonArray rulesArray = new JsonArray();
        root.add("rules", rulesArray);
        Files.writeString(configFile, GSON.toJson(root));
    }

    public List<TextureReplacementRule> getRules() {
        return rules;
    }
}