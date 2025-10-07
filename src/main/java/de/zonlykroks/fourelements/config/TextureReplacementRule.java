package de.zonlykroks.fourelements.config;

import com.google.gson.JsonObject;
import net.minecraft.block.BlockState;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public record TextureReplacementRule(List<String> targetBlocks, List<PositionCondition> positionConditions,
                                     List<NeighborCondition> neighborConditions, List<BlockStateCondition> blockStateConditions,
                                     String replacementTexture) {
    public TextureReplacementRule(List<String> targetBlocks,
                                  List<PositionCondition> positionConditions,
                                  List<NeighborCondition> neighborConditions,
                                  List<BlockStateCondition> blockStateConditions,
                                  String replacementTexture) {
        this.targetBlocks = targetBlocks != null ? targetBlocks : new ArrayList<>();
        this.positionConditions = positionConditions != null ? positionConditions : new ArrayList<>();
        this.neighborConditions = neighborConditions != null ? neighborConditions : new ArrayList<>();
        this.blockStateConditions = blockStateConditions != null ? blockStateConditions : new ArrayList<>();
        this.replacementTexture = replacementTexture;
    }

    public boolean matches(BlockRenderView world, BlockPos pos, BlockState state) {
        if (!targetBlocks.isEmpty()) {
            Identifier blockId = Identifier.of(state.getBlock().getTranslationKey());
            boolean matchesAny = false;
            for (String target : targetBlocks) {
                if (blockId.toString().contains(target) || state.getBlock().getTranslationKey().contains(target)) {
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

        for (NeighborCondition condition : neighborConditions) {
            if (!condition.test(world, pos)) {
                return false;
            }
        }

        for (BlockStateCondition condition : blockStateConditions) {
            if (!condition.test(state)) {
                return false;
            }
        }

        return true;
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

            // Check neighbor blockstate conditions
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

    public record BlockStateCondition(String property, String value) {
        public boolean test(BlockState state) {
            var property = state.getProperties().stream()
                    .filter(prop -> prop.getName().equals(this.property))
                    .findFirst()
                    .orElse(null);

            if (property == null) {
                return false;
            }

            var currentValue = state.get(property);
            return currentValue.toString().equalsIgnoreCase(value);
        }

        public static BlockStateCondition fromJson(JsonObject json) {
            String property = json.get("property").getAsString();
            String value = json.get("value").getAsString();
            return new BlockStateCondition(property, value);
        }
    }
}