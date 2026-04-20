package com.ultra.megamod.feature.combat.animation.client.particle;

import com.ultra.megamod.feature.combat.animation.api.fx.Color;
import com.ultra.megamod.feature.combat.animation.particle.BetterCombatParticles;
import com.ultra.megamod.feature.combat.animation.particle.SlashParticleEffect;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.state.QuadParticleRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

/**
 * Renders weapon slash trail particles with custom orientation and color.
 * Ported 1:1 from BetterCombat (net.bettercombat.client.particle.SlashParticle).
 */
public class SlashParticle extends SingleQuadParticle {
    private final SpriteSet spriteProvider;
    public final float modelOffset;
    private final float pitch;
    private final float yaw;
    private final float localYaw;
    private final boolean light;

    public SlashParticle(ClientLevel world, double x, double y, double z, float scale,
                         float pitch, float yaw, float localYaw, float roll, boolean light,
                         long color_rgba, SpriteSet spriteProvider) {
        super(world, x, y, z, spriteProvider.get(0, 1));
        this.spriteProvider = spriteProvider;
        this.light = light;
        this.pitch = pitch;
        this.yaw = yaw;
        this.roll = roll;
        this.oRoll = roll;
        this.localYaw = localYaw;

        var color = Color.fromRGBA(color_rgba);
        this.setColor(color.red(), color.green(), color.blue());
        this.alpha = color.alpha();

        this.lifetime = 6;
        this.modelOffset = this.setModelOffset();
        this.quadSize = scale;
        this.setSpriteFromAge(spriteProvider);
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
        } else {
            this.setSpriteFromAge(this.spriteProvider);
        }
    }

    @Override
    protected int getLightColor(float partialTick) {
        BlockPos blockPos = BlockPos.containing(this.x, this.y, this.z);
        if (this.light) {
            return 15728880;
        } else {
            return this.level.hasChunkAt(blockPos) ? LevelRenderer.getLightColor(this.level, blockPos) : 0;
        }
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        return SingleQuadParticle.Layer.TRANSLUCENT;
    }

    public float setModelOffset() {
        return 0.0F;
    }

    /**
     * Custom rotation + two-sided quad rendering, ported from source BetterCombat's
     * SlashParticle.render(). The source builds a rotation matrix from
     * yaw / (pitch+90) / roll / localYaw and renders both front + back faces with
     * swapped U coordinates so the slash texture is visible from either side.
     *
     * <p>In 1.21.11 {@code SingleQuadParticle.extract} writes into a batched
     * {@link QuadParticleRenderState}; we bypass {@link #getFacingCameraMode()}
     * (which would produce a camera-aligned billboard — the cause of "trails all
     * over the place") and emit two quads directly with the weapon-swing rotation.</p>
     */
    @Override
    public void extract(QuadParticleRenderState reusedState, Camera camera, float partialTick) {
        // Build the source rotation matrix: Y(-yaw) × X(pitch+90) × Y(roll) × Z(localYaw)
        Matrix4f rotationMatrix = new Matrix4f().identity();
        rotationMatrix.rotateY((float) Math.toRadians(-this.yaw));
        rotationMatrix.rotateX((float) Math.toRadians(this.pitch + 90.0F));
        rotationMatrix.rotateY((float) Math.toRadians(this.roll));
        rotationMatrix.rotateZ((float) Math.toRadians(this.localYaw));

        Quaternionf rotation = new Quaternionf();
        rotation.setFromNormalized(rotationMatrix);

        // Camera-relative position (matches source's this.lastX - cameraPos.x)
        Vec3 cameraPos = camera.position();
        float x = (float) (Mth.lerp((double) partialTick, this.xo, this.x) - cameraPos.x());
        float y = (float) (Mth.lerp((double) partialTick, this.yo, this.y) - cameraPos.y()) + this.modelOffset;
        float z = (float) (Mth.lerp((double) partialTick, this.zo, this.z) - cameraPos.z());

        float size = this.getQuadSize(partialTick);
        int color = ARGB.colorFromFloat(this.alpha, this.rCol, this.gCol, this.bCol);
        int light = this.getLightColor(partialTick);

        float minU = this.getU0();
        float maxU = this.getU1();
        float minV = this.getV0();
        float maxV = this.getV1();

        // Front face (U flipped: maxU→minU so texture orients correctly)
        reusedState.add(
                this.getLayer(),
                x, y, z,
                rotation.x, rotation.y, rotation.z, rotation.w,
                size,
                maxU, minU, minV, maxV,
                color, light);

        // Back face: rotate 180° around Y so the quad's reverse side points at the viewer,
        // with un-flipped UVs so the same texture shows correctly from behind.
        Quaternionf back = new Quaternionf(rotation).rotateY((float) Math.PI);
        reusedState.add(
                this.getLayer(),
                x, y, z,
                back.x, back.y, back.z, back.w,
                size,
                minU, maxU, minV, maxV,
                color, light);
    }

    public static class Provider implements ParticleProvider<SlashParticleEffect> {
        private final SpriteSet spriteProvider;
        private final BetterCombatParticles.StaticParams params;

        public Provider(SpriteSet spriteProvider, BetterCombatParticles.StaticParams params) {
            this.spriteProvider = spriteProvider;
            this.params = params;
        }

        @Override
        public @Nullable Particle createParticle(SlashParticleEffect settings, ClientLevel clientWorld,
                                                  double x, double y, double z,
                                                  double velocityX, double velocityY, double velocityZ,
                                                  RandomSource random) {
            return new SlashParticle(clientWorld, x, y, z,
                    settings.getScale(), settings.getPitch(), settings.getYaw(),
                    settings.getLocalYaw(), settings.getRoll(), settings.getLight(),
                    settings.getColorRGBA(), this.spriteProvider);
        }
    }
}
