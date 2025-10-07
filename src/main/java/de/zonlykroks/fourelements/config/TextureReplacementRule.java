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
                                     List<NeighborCondition> neighborConditions, String replacementTexture) {
    public TextureReplacementRule(List<String> targetBlocks,
                                  List<PositionCondition> positionConditions,
                                  List<NeighborCondition> neighborConditions,
                                  String replacementTexture) {
        this.targetBlocks = targetBlocks != null ? targetBlocks : new ArrayList<>();
        this.positionConditions = positionConditions != null ? positionConditions : new ArrayList<>();
        this.neighborConditions = neighborConditions != null ? neighborConditions : new ArrayList<>();
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

    public record NeighborCondition(Direction direction, int offsetX, int offsetY, int offsetZ, String targetBlock) {
        public NeighborCondition(@Nullable Direction direction, int offsetX, int offsetY, int offsetZ, String targetBlock) {
            this.direction = direction;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            this.offsetZ = offsetZ;
            this.targetBlock = targetBlock;
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

            return neighborId.contains(targetBlock);
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

            return new NeighborCondition(direction, offsetX, offsetY, offsetZ, targetBlock);
        }
    }
}