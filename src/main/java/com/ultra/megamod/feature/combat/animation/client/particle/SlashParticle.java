package com.ultra.megamod.feature.combat.animation.client.particle;

import com.ultra.megamod.feature.combat.animation.api.fx.Color;
import com.ultra.megamod.feature.combat.animation.particle.BetterCombatParticles;
import com.ultra.megamod.feature.combat.animation.particle.SlashParticleEffect;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;

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
