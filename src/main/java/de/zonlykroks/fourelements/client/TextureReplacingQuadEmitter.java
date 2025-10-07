package de.zonlykroks.fourelements.client;

import net.fabricmc.fabric.api.renderer.v1.mesh.*;
import net.fabricmc.fabric.api.renderer.v1.model.SpriteFinder;
import net.fabricmc.fabric.api.renderer.v1.sprite.FabricSpriteAtlasTexture;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BlockRenderLayer;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.util.Atlases;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class TextureReplacingQuadEmitter implements QuadEmitter {
    private final QuadEmitter delegate;
    private final BlockRenderView blockView;
    private final BlockPos pos;
    private final BlockState state;
    private final SpriteFinder spriteFinder;

    public TextureReplacingQuadEmitter(QuadEmitter delegate, BlockRenderView blockView, BlockPos pos, BlockState state) {
        this.delegate = delegate;
        this.blockView = blockView;
        this.pos = pos;
        this.state = state;

        SpriteAtlasTexture atlas = MinecraftClient.getInstance().getAtlasManager().getAtlasTexture(Atlases.BLOCKS);
        this.spriteFinder = atlas.spriteFinder();
    }

    @Override
    public QuadEmitter emit() {
        Sprite currentSprite = spriteFinder.find(delegate);

        Sprite replacementSprite = TextureReplacementManager.getInstance()
                .getReplacementSprite(blockView, pos, state, currentSprite);

        if (replacementSprite != null && replacementSprite != currentSprite) {
            float[] u = new float[4];
            float[] v = new float[4];

            for (int i = 0; i < 4; i++) {
                float atlasU = delegate.u(i);
                float atlasV = delegate.v(i);

                u[i] = (atlasU - currentSprite.getMinU()) / (currentSprite.getMaxU() - currentSprite.getMinU());
                v[i] = (atlasV - currentSprite.getMinV()) / (currentSprite.getMaxV() - currentSprite.getMinV());
            }

            for (int i = 0; i < 4; i++) {
                float newU = replacementSprite.getMinU() + u[i] * (replacementSprite.getMaxU() - replacementSprite.getMinU());
                float newV = replacementSprite.getMinV() + v[i] * (replacementSprite.getMaxV() - replacementSprite.getMinV());
                delegate.uv(i, newU, newV);
            }
        }

        return delegate.emit();
    }

    @Override
    public QuadEmitter pos(int vertexIndex, float x, float y, float z) {
        delegate.pos(vertexIndex, x, y, z);
        return this;
    }

    @Override
    public QuadEmitter color(int vertexIndex, int color) {
        delegate.color(vertexIndex, color);
        return this;
    }

    @Override
    public QuadEmitter uv(int vertexIndex, float u, float v) {
        delegate.uv(vertexIndex, u, v);
        return this;
    }

    @Override
    public QuadEmitter lightmap(int vertexIndex, int lightmap) {
        delegate.lightmap(vertexIndex, lightmap);
        return this;
    }

    @Override
    public QuadEmitter normal(int vertexIndex, float x, float y, float z) {
        delegate.normal(vertexIndex, x, y, z);
        return this;
    }

    @Override
    public QuadEmitter nominalFace(@Nullable Direction direction) {
        delegate.nominalFace(direction);
        return this;
    }

    @Override
    public QuadEmitter cullFace(@Nullable Direction direction) {
        delegate.cullFace(direction);
        return this;
    }

    @Override
    public QuadEmitter renderLayer(@Nullable BlockRenderLayer blockRenderLayer) {
        delegate.renderLayer(blockRenderLayer);
        return this;
    }

    @Override
    public QuadEmitter emissive(boolean b) {
        delegate.emissive(b);
        return this;
    }

    @Override
    public QuadEmitter diffuseShade(boolean b) {
        delegate.diffuseShade(b);
        return this;
    }

    @Override
    public QuadEmitter ambientOcclusion(TriState triState) {
        delegate.ambientOcclusion(triState);
        return this;
    }

    @Override
    public QuadEmitter glint(ItemRenderState.@Nullable Glint glint) {
        delegate.glint(glint);
        return this;
    }

    @Override
    public QuadEmitter shadeMode(ShadeMode shadeMode) {
        delegate.shadeMode(shadeMode);
        return this;
    }

    @Override
    public QuadEmitter tintIndex(int i) {
        delegate.tintIndex(i);
        return this;
    }

    @Override
    public QuadEmitter tag(int tag) {
        delegate.tag(tag);
        return this;
    }

    @Override
    public QuadEmitter copyFrom(QuadView quadView) {
        delegate.copyFrom(quadView);
        return this;
    }

    @Override
    public QuadEmitter fromVanilla(int[] ints, int i) {
        delegate.fromVanilla(ints, i);
        return this;
    }

    @Override
    public QuadEmitter fromBakedQuad(BakedQuad bakedQuad) {
        delegate.fromBakedQuad(bakedQuad);
        return this;
    }

    @Override
    public QuadEmitter spriteBake(Sprite sprite, int bakeFlags) {
        delegate.spriteBake(sprite, bakeFlags);
        return this;
    }

    @Override
    public QuadEmitter square(net.minecraft.util.math.Direction nominalFace, float left, float bottom, float right, float top, float depth) {
        delegate.square(nominalFace, left, bottom, right, top, depth);
        return this;
    }

    @Override
    public void pushTransform(QuadTransform quadTransform) {
        delegate.pushTransform(quadTransform);
    }

    @Override
    public void popTransform() {
        delegate.popTransform();
    }

    @Override
    public float x(int vertexIndex) {
        return delegate.x(vertexIndex);
    }

    @Override
    public float y(int vertexIndex) {
        return delegate.y(vertexIndex);
    }

    @Override
    public float z(int vertexIndex) {
        return delegate.z(vertexIndex);
    }

    @Override
    public float posByIndex(int i, int i1) {
        return delegate.posByIndex(i, i1);
    }

    @Override
    public Vector3f copyPos(int i, @Nullable Vector3f vector3f) {
        return delegate.copyPos(i, vector3f);
    }

    @Override
    public int color(int vertexIndex) {
        return delegate.color(vertexIndex);
    }

    @Override
    public float u(int vertexIndex) {
        return delegate.u(vertexIndex);
    }

    @Override
    public float v(int vertexIndex) {
        return delegate.v(vertexIndex);
    }

    @Override
    public Vector2f copyUv(int i, @Nullable Vector2f vector2f) {
        return delegate.copyUv(i, vector2f);
    }

    @Override
    public int lightmap(int vertexIndex) {
        return delegate.lightmap(vertexIndex);
    }

    @Override
    public boolean hasNormal(int vertexIndex) {
        return delegate.hasNormal(vertexIndex);
    }

    @Override
    public float normalX(int vertexIndex) {
        return delegate.normalX(vertexIndex);
    }

    @Override
    public float normalY(int vertexIndex) {
        return delegate.normalY(vertexIndex);
    }

    @Override
    public float normalZ(int vertexIndex) {
        return delegate.normalZ(vertexIndex);
    }

    @Override
    public @Nullable Vector3f copyNormal(int i, @Nullable Vector3f vector3f) {
        return delegate.copyNormal(i, vector3f);
    }

    @Override
    public Vector3fc faceNormal() {
        return delegate.faceNormal();
    }

    @Override
    public @NotNull Direction lightFace() {
        return delegate.lightFace();
    }

    @Override
    public @Nullable Direction nominalFace() {
        return delegate.nominalFace();
    }

    @Override
    public @Nullable Direction cullFace() {
        return delegate.cullFace();
    }

    @Override
    public @Nullable BlockRenderLayer renderLayer() {
        return delegate.renderLayer();
    }

    @Override
    public boolean emissive() {
        return delegate.emissive();
    }

    @Override
    public boolean diffuseShade() {
        return delegate.diffuseShade();
    }

    @Override
    public TriState ambientOcclusion() {
        return delegate.ambientOcclusion();
    }

    @Override
    public ItemRenderState.@Nullable Glint glint() {
        return delegate.glint();
    }

    @Override
    public ShadeMode shadeMode() {
        return delegate.shadeMode();
    }

    @Override
    public int tintIndex() {
        return delegate.tintIndex();
    }

    @Override
    public int tag() {
        return delegate.tag();
    }

    @Override
    public void toVanilla(int[] target, int targetIndex) {
        delegate.toVanilla(target, targetIndex);
    }
}