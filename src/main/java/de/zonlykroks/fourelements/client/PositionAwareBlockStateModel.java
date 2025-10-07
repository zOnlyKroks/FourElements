package de.zonlykroks.fourelements.client;

import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBlockStateModel;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BlockModelPart;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

public record PositionAwareBlockStateModel(BlockStateModel wrapped) implements BlockStateModel, FabricBlockStateModel {

    @Override
    public void emitQuads(QuadEmitter emitter, BlockRenderView blockView, BlockPos pos, BlockState state,
                          Random random, Predicate<@Nullable Direction> cullTest) {
        QuadEmitter wrappedEmitter = new TextureReplacingQuadEmitter(emitter, blockView, pos, state);

        if (wrapped instanceof FabricBlockStateModel fabricModel) {
            fabricModel.emitQuads(wrappedEmitter, blockView, pos, state, random, cullTest);
        } else {
            final List<BlockModelPart> parts = wrapped.getParts(random);

            for (BlockModelPart part : parts) {
                part.emitQuads(wrappedEmitter, cullTest);
            }
        }
    }

    @Override
    public @Nullable Object createGeometryKey(BlockRenderView blockView, BlockPos pos, BlockState state, Random random) {
        return null;
    }

    @Override
    public Sprite particleSprite(BlockRenderView blockView, BlockPos pos, BlockState state) {
        if (wrapped instanceof FabricBlockStateModel fabricModel) {
            return fabricModel.particleSprite(blockView, pos, state);
        }
        return wrapped.particleSprite();
    }

    @Override
    public List<BlockModelPart> getParts(Random random) {
        return wrapped.getParts(random);
    }

    @Override
    public Sprite particleSprite() {
        return wrapped.particleSprite();
    }

    @Override
    public void addParts(Random random, List<BlockModelPart> parts) {
        wrapped.addParts(random, parts);
    }
}