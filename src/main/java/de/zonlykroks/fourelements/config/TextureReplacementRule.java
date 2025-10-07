package de.zonlykroks.fourelements.config;

import com.google.gson.JsonObject;
import net.minecraft.block.BlockState;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public record TextureReplacementRule(List<String> targetBlocks, List<PositionCondition> positionConditions,
                                     List<NeighborCondition> neighborConditions, List<BlockStateCondition> blockStateConditions,
                                     String replacementTexture, Identifier parsedTextureId) {
    private static final Logger LOGGER = LoggerFactory.getLogger("FourElements");

    public TextureReplacementRule(List<String> targetBlocks,
                                  List<PositionCondition> positionConditions,
                                  List<NeighborCondition> neighborConditions,
                                  List<BlockStateCondition> blockStateConditions,
                                  String replacementTexture) {
        this(targetBlocks != null ? targetBlocks : new ArrayList<>(),
             positionConditions != null ? positionConditions : new ArrayList<>(),
             neighborConditions != null ? neighborConditions : new ArrayList<>(),
             blockStateConditions != null ? blockStateConditions : new ArrayList<>(),
             replacementTexture,
             parseTextureIdentifier(replacementTexture));
    }

    private static Identifier parseTextureIdentifier(String textureId) {
        if (textureId.contains(":")) {
            return Identifier.tryParse(textureId);
        } else {
            if (!textureId.startsWith("block/")) {
                textureId = "block/" + textureId;
            }
            return Identifier.of("minecraft", textureId);
        }
    }

    public boolean matches(BlockRenderView world, BlockPos pos, BlockState state) {
        if (!targetBlocks.isEmpty()) {
            String translationKey = state.getBlock().getTranslationKey();
            boolean matchesAny = false;
            for (String target : targetBlocks) {
                if (translationKey.contains(target)) {
                    matchesAny = true;
                    break;
                }
            }
            if (!matchesAny) {
                return false;
            }
        }

        for (PositionCondition condition : positionConditions) {
            if (!condition.test(pos)) {
                return false;
            }
        }

        for (BlockStateCondition condition : blockStateConditions) {
            if (!condition.test(world, pos)) {
                return false;
            }
        }

        for (NeighborCondition condition : neighborConditions) {
            if (!condition.test(world, pos)) {
                return false;
            }
        }

        return true;
    }

    public Sprite getReplacementSprite(SpriteAtlasTexture atlas) {
        if (parsedTextureId == null) {
            LOGGER.warn("Invalid texture identifier: {}", replacementTexture);
            return null;
        }

        try {
            Sprite sprite = atlas.getSprite(parsedTextureId);

            if (sprite != null && !sprite.getContents().getId().toString().contains("missingno")) {
                return sprite;
            } else {
                LOGGER.warn("Sprite not found in atlas: {} (got missingno)", parsedTextureId);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to get replacement sprite for {}: {}", parsedTextureId, e.getMessage());
        }

        return null;
    }

    public boolean hasNeighborConditions() {
        return !neighborConditions.isEmpty();
    }

    public record PositionCondition(String axis, String operator, int value, Integer modulo) {
        public PositionCondition(String axis, String operator, int value, @Nullable Integer modulo) {
            this.axis = axis;
            this.operator = operator;
            this.value = value;
            this.modulo = modulo;
        }

        public boolean test(BlockPos pos) {
            int coordinate = switch (axis.toLowerCase()) {
                case "x" -> pos.getX();
                case "y" -> pos.getY();
                case "z" -> pos.getZ();
                default -> throw new IllegalArgumentException("Invalid axis: " + axis);
            };

            if (operator.equals("%") && modulo != null) {
                return coordinate % modulo == value;
            }

            return switch (operator) {
                case "==" -> coordinate == value;
                case ">" -> coordinate > value;
                case "<" -> coordinate < value;
                case ">=" -> coordinate >= value;
                case "<=" -> coordinate <= value;
                default -> throw new IllegalArgumentException("Invalid operator: " + operator);
            };
        }

        public static PositionCondition fromJson(JsonObject json) {
            String axis = json.get("axis").getAsString();
            String operator = json.get("operator").getAsString();
            int value = json.get("value").getAsInt();
            Integer modulo = json.has("modulo") ? json.get("modulo").getAsInt() : null;
            return new PositionCondition(axis, operator, value, modulo);
        }
    }

    public record NeighborCondition(Direction direction, int offsetX, int offsetY, int offsetZ, String targetBlock,
                                    List<BlockStateCondition> blockStateConditions) {
        public NeighborCondition(@Nullable Direction direction, int offsetX, int offsetY, int offsetZ, String targetBlock,
                                 @Nullable List<BlockStateCondition> blockStateConditions) {
            this.direction = direction;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            this.offsetZ = offsetZ;
            this.targetBlock = targetBlock;
            this.blockStateConditions = blockStateConditions != null ? blockStateConditions : new ArrayList<>();
        }

        public boolean test(BlockRenderView world, BlockPos pos) {
            BlockPos checkPos;
            if (direction != null) {
                checkPos = pos.offset(direction);
            } else {
                checkPos = pos.add(offsetX, offsetY, offsetZ);
            }

            BlockState neighborState = world.getBlockState(checkPos);
            String neighborId = neighborState.getBlock().getTranslationKey();

            if (!neighborId.contains(targetBlock)) {
                return false;
            }

            for (BlockStateCondition condition : blockStateConditions) {
                if (!condition.test(neighborState)) {
                    return false;
                }
            }

            return true;
        }

        public static NeighborCondition fromJson(JsonObject json) {
            Direction direction = null;
            if (json.has("direction")) {
                direction = Direction.valueOf(json.get("direction").getAsString());
            }

            int offsetX = json.has("offsetX") ? json.get("offsetX").getAsInt() : 0;
            int offsetY = json.has("offsetY") ? json.get("offsetY").getAsInt() : 0;
            int offsetZ = json.has("offsetZ") ? json.get("offsetZ").getAsInt() : 0;
            String targetBlock = json.get("targetBlock").getAsString();

            List<BlockStateCondition> blockStateConditions = new ArrayList<>();
            if (json.has("blockStateConditions")) {
                var conditionsArray = json.getAsJsonArray("blockStateConditions");
                for (int i = 0; i < conditionsArray.size(); i++) {
                    blockStateConditions.add(BlockStateCondition.fromJson(
                            conditionsArray.get(i).getAsJsonObject()));
                }
            }

            return new NeighborCondition(direction, offsetX, offsetY, offsetZ, targetBlock, blockStateConditions);
        }
    }

    public record BlockStateCondition(String property, String value, @Nullable Direction direction,
                                      int offsetX, int offsetY, int offsetZ) {
        public BlockStateCondition(String property, String value) {
            this(property, value, null, 0, 0, 0);
        }

        public BlockStateCondition(String property, String value, @Nullable Direction direction,
                                  int offsetX, int offsetY, int offsetZ) {
            this.property = property;
            this.value = value;
            this.direction = direction;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            this.offsetZ = offsetZ;
        }

        public boolean test(BlockState state) {
            for (var prop : state.getProperties()) {
                if (prop.getName().equals(this.property)) {
                    var currentValue = state.get(prop);
                    return currentValue.toString().equalsIgnoreCase(value);
                }
            }
            return false;
        }

        public boolean test(BlockRenderView world, BlockPos pos) {
            BlockPos checkPos;
            if (direction != null) {
                checkPos = pos.offset(direction);
            } else if (offsetX != 0 || offsetY != 0 || offsetZ != 0) {
                checkPos = pos.add(offsetX, offsetY, offsetZ);
            } else {
                checkPos = pos;
            }

            BlockState state = world.getBlockState(checkPos);
            return test(state);
        }

        public static BlockStateCondition fromJson(JsonObject json) {
            String property = json.get("property").getAsString();
            String value = json.get("value").getAsString();

            Direction direction = null;
            if (json.has("direction")) {
                direction = Direction.valueOf(json.get("direction").getAsString());
            }

            int offsetX = json.has("offsetX") ? json.get("offsetX").getAsInt() : 0;
            int offsetY = json.has("offsetY") ? json.get("offsetY").getAsInt() : 0;
            int offsetZ = json.has("offsetZ") ? json.get("offsetZ").getAsInt() : 0;

            return new BlockStateCondition(property, value, direction, offsetX, offsetY, offsetZ);
        }
    }
}