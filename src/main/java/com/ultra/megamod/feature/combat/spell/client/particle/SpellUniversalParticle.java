package com.ultra.megamod.feature.combat.spell.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.entity.Entity;
import javax.annotation.Nullable;

/**
 * Configurable universal spell particle with 4 motion types and entity following.
 * Ported 1:1 from SpellEngine (net.spell_engine.client.particle.SpellUniversalParticle).
 */
public class SpellUniversalParticle extends SingleQuadParticle {
    private final SpriteSet sprites;
    private final SpellParticleEnums.Motion motion;
    private boolean animated = false;
    public boolean glows = true;
    public boolean translucent = true;
    @Nullable Entity followEntity;

    SpellUniversalParticle(ClientLevel level, SpriteSet sprites, SpellParticleEnums.Motion motion,
                           double x, double y, double z, double xd, double yd, double zd) {
        super(level, x, y, z, 0.5 - level.random.nextDouble(), yd, 0.5 - level.random.nextDouble(), sprites.get(level.random));
        this.sprites = sprites;
        this.motion = motion;

        switch (motion) {
            case FLOAT, DECELERATE -> {
                this.friction = 0.96F;
                this.xd = this.xd * 0.01 + xd;
                this.yd = this.yd * 0.01 + yd;
                this.zd = this.zd * 0.01 + zd;
                this.x += (this.random.nextFloat() - this.random.nextFloat()) * 0.05;
                this.y += (this.random.nextFloat() - this.random.nextFloat()) * 0.05;
                this.z += (this.random.nextFloat() - this.random.nextFloat()) * 0.05;
                if (motion == SpellParticleEnums.Motion.DECELERATE) {
                    this.friction *= 0.8F;
                }
                this.lifetime = (int) (8.0 / (Math.random() * 0.8 + 0.2));
            }
            case ASCEND -> {
                this.friction = 0.96F;
                this.gravity = -0.1F;
                this.yd *= 0.2;
                if (xd == 0.0 && zd == 0.0) {
                    this.xd *= 0.1;
                    this.zd *= 0.1;
                }
                this.lifetime = (int) (8.0 / (Math.random() * 0.8 + 0.2));
            }
            case BURST -> {
                this.friction = 0.7f;
                this.gravity = 0.5f;
                this.xd *= 0.1;
                this.yd *= 0.1;
                this.zd *= 0.1;
                this.xd += xd * 0.4;
                this.yd += yd * 0.4;
                this.zd += zd * 0.4;
                this.lifetime = Math.max((int) (6.0 / (Math.random() * 0.8 + 0.6)), 1);
            }
        }

        this.setSpriteFromAge(sprites);
        this.hasPhysics = false;
    }

    @Override
    public net.minecraft.client.particle.SingleQuadParticle.Layer getLayer() {
        if (glows) {
            return translucent ? net.minecraft.client.particle.SingleQuadParticle.Layer.TRANSLUCENT : net.minecraft.client.particle.SingleQuadParticle.Layer.TRANSLUCENT;
        } else {
            return translucent ? net.minecraft.client.particle.SingleQuadParticle.Layer.TRANSLUCENT : net.minecraft.client.particle.SingleQuadParticle.Layer.OPAQUE;
        }
    }

    @Override
    public int getLightColor(float tint) {
        return glows ? 0xF000F0 : super.getLightColor(tint);
    }

    @Override
    public void move(double dx, double dy, double dz) {
        if (followEntity != null && !followEntity.isRemoved()) {
            dx += followEntity.getX() - followEntity.xOld;
            dy += followEntity.getY() - followEntity.yOld;
            dz += followEntity.getZ() - followEntity.zOld;
        }
        super.move(dx, dy, dz);
    }

    @Override
    public void tick() {
        super.tick();
        if (animated) {
            this.setSpriteFromAge(this.sprites);
        }
    }

    // ═══════════════════════════════════════════
    // Factory — TemplateParticleType variant
    // ═══════════════════════════════════════════

    public static class MagicVariant implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;
        private final SpellParticleEnums.MagicVariant variant;

        public MagicVariant(SpriteSet sprites, SpellParticleEnums.MagicVariant variant) {
            this.sprites = sprites;
            this.variant = variant;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                        double xd, double yd, double zd, net.minecraft.util.RandomSource random) {
            var p = new SpellUniversalParticle(level, this.sprites, variant.motion(), x, y, z, xd, yd, zd);
            p.glows = true;
            p.animated = variant.shape().animated;
            p.rCol = 1F; p.gCol = 1F; p.bCol = 1F;
            p.quadSize *= 0.75f;

            switch (variant.shape()) {
                case SPELL, STRIPE -> p.alpha = 1F;
                default -> p.alpha = 0.75F;
            }

            TemplateParticleType.Appearance appearance = TemplateParticleType.consumeAppearance();
            if (appearance != null) {
                TemplateParticleType.apply(appearance, p);
                if (appearance.color != null) {
                    p.alpha *= appearance.color.alpha();
                }
                p.quadSize *= appearance.scale;
                p.lifetime = (int) (p.lifetime * appearance.max_age);
                p.followEntity = appearance.entityFollowed;
            }

            float j = level.random.nextFloat() * 0.5F + 0.35F;
            p.setColor(p.rCol * j, p.gCol * j, p.bCol * j);
            return p;
        }
    }
}
