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
import java.util.List;

public class TextureReplacementConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("FourElements");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir().resolve("fourelements");
    private static final Path CONFIG_FILE = CONFIG_DIR.resolve("texture_replacements.json");

    private final List<TextureReplacementRule> rules = new ArrayList<>();

    public void load() {
        try {
            if (!Files.exists(CONFIG_DIR)) {
                Files.createDirectories(CONFIG_DIR);
                LOGGER.info("Created FourElements config directory at: {}", CONFIG_DIR);
            }

            if (!Files.exists(CONFIG_FILE)) {
                createDefaultConfig();
                LOGGER.info("Created default texture replacement config at: {}", CONFIG_FILE);
            }

            String json = Files.readString(CONFIG_FILE);
            JsonObject root = GSON.fromJson(json, JsonObject.class);
            JsonArray rulesArray = root.getAsJsonArray("rules");

            rules.clear();
            for (int i = 0; i < rulesArray.size(); i++) {
                JsonObject ruleJson = rulesArray.get(i).getAsJsonObject();
                TextureReplacementRule rule = parseRule(ruleJson);
                rules.add(rule);
            }

            LOGGER.info("Loaded {} texture replacement rules", rules.size());
        } catch (IOException e) {
            LOGGER.error("Failed to load texture replacement config", e);
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

    private void createDefaultConfig() throws IOException {
        JsonObject root = new JsonObject();
        JsonArray rulesArray = new JsonArray();

        JsonObject rule1 = new JsonObject();
        JsonArray targetBlocks1 = new JsonArray();
        targetBlocks1.add("stone");
        rule1.add("targetBlocks", targetBlocks1);

        JsonArray posConditions1 = new JsonArray();
        JsonObject posCondition1 = new JsonObject();
        posCondition1.addProperty("axis", "y");
        posCondition1.addProperty("operator", "%");
        posCondition1.addProperty("value", 0);
        posCondition1.addProperty("modulo", 2);
        posConditions1.add(posCondition1);
        rule1.add("positionConditions", posConditions1);

        rule1.addProperty("replacementTexture", "minecraft:block/diamond_block");
        rulesArray.add(rule1);

        JsonObject rule2 = new JsonObject();
        JsonArray targetBlocks2 = new JsonArray();
        targetBlocks2.add("dirt");
        rule2.add("targetBlocks", targetBlocks2);

        JsonArray neighborConditions2 = new JsonArray();
        JsonObject neighborCondition2 = new JsonObject();
        neighborCondition2.addProperty("direction", "UP");
        neighborCondition2.addProperty("targetBlock", "water");
        neighborConditions2.add(neighborCondition2);
        rule2.add("neighborConditions", neighborConditions2);

        rule2.addProperty("replacementTexture", "minecraft:block/coarse_dirt");
        rulesArray.add(rule2);

        JsonObject rule3 = new JsonObject();
        JsonArray targetBlocks3 = new JsonArray();
        targetBlocks3.add("grass_block");
        rule3.add("targetBlocks", targetBlocks3);

        JsonArray posConditions3 = new JsonArray();
        JsonObject posCondition3 = new JsonObject();
        posCondition3.addProperty("axis", "x");
        posCondition3.addProperty("operator", "%");
        posCondition3.addProperty("value", 0);
        posCondition3.addProperty("modulo", 3);
        posConditions3.add(posCondition3);
        rule3.add("positionConditions", posConditions3);

        rule3.addProperty("replacementTexture", "minecraft:block/moss_block");
        rulesArray.add(rule3);

        JsonObject rule4 = new JsonObject();
        JsonArray targetBlocks4 = new JsonArray();
        targetBlocks4.add("oak_planks");
        rule4.add("targetBlocks", targetBlocks4);

        JsonArray posConditions4 = new JsonArray();
        JsonObject posCondition4a = new JsonObject();
        posCondition4a.addProperty("axis", "y");
        posCondition4a.addProperty("operator", ">=");
        posCondition4a.addProperty("value", 64);
        posConditions4.add(posCondition4a);

        JsonObject posCondition4b = new JsonObject();
        posCondition4b.addProperty("axis", "y");
        posCondition4b.addProperty("operator", "<=");
        posCondition4b.addProperty("value", 70);
        posConditions4.add(posCondition4b);
        rule4.add("positionConditions", posConditions4);

        rule4.addProperty("replacementTexture", "minecraft:block/spruce_planks");
        rulesArray.add(rule4);

        JsonObject rule5 = new JsonObject();
        JsonArray targetBlocks5 = new JsonArray();
        targetBlocks5.add("emerald_ore");
        rule5.add("targetBlocks", targetBlocks5);

        JsonArray neighborConditions5 = new JsonArray();
        JsonObject neighborCondition5 = new JsonObject();
        neighborCondition5.addProperty("direction", "NORTH");
        neighborCondition5.addProperty("targetBlock", "repeater");

        JsonArray blockStateConditions5 = new JsonArray();
        JsonObject blockStateCondition5 = new JsonObject();
        blockStateCondition5.addProperty("property", "delay");
        blockStateCondition5.addProperty("value", "4");
        blockStateConditions5.add(blockStateCondition5);
        neighborCondition5.add("blockStateConditions", blockStateConditions5);

        neighborConditions5.add(neighborCondition5);
        rule5.add("neighborConditions", neighborConditions5);

        rule5.addProperty("replacementTexture", "fourelements:block/replacement_texture");
        rulesArray.add(rule5);

        root.add("rules", rulesArray);

        Files.writeString(CONFIG_FILE, GSON.toJson(root));
    }

    public List<TextureReplacementRule> getRules() {
        return rules;
    }
}