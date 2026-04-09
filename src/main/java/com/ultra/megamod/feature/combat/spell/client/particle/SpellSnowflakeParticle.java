package com.ultra.megamod.feature.combat.spell.client.particle;

import com.ultra.megamod.feature.combat.spell.client.util.Color;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
/**
 * Snowflake-based particle with glow support and multiple color variants.
 * Ported 1:1 from SpellEngine (net.spell_engine.client.particle.SpellSnowflakeParticle).
 */
public class SpellSnowflakeParticle extends SingleQuadParticle {
    boolean glow = true;
    private final SpriteSet sprites;

    protected SpellSnowflakeParticle(ClientLevel level, double x, double y, double z,
                                     double xd, double yd, double zd, SpriteSet sprites) {
        super(level, x, y, z, xd, yd, zd, sprites.get(level.random));
        this.sprites = sprites;
        this.gravity = 0.225F;
        this.friction = 1.0F;
        this.xd = xd + (Math.random() * 2.0 - 1.0) * 0.05;
        this.yd = yd + (Math.random() * 2.0 - 1.0) * 0.05;
        this.zd = zd + (Math.random() * 2.0 - 1.0) * 0.05;
        this.quadSize *= 0.75F;
        this.lifetime = (int) (8.0 / (Math.random() * 0.8 + 0.2));
        this.setSpriteFromAge(sprites);
        this.hasPhysics = false;
    }

    @Override
    public net.minecraft.client.particle.SingleQuadParticle.Layer getLayer() {
        return net.minecraft.client.particle.SingleQuadParticle.Layer.TRANSLUCENT;
    }

    @Override
    public int getLightColor(float tint) {
        return glow ? 0xF000F0 : super.getLightColor(tint);
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(this.sprites);
    }

    // ═══════════════════════════════════════════
    // Factories
    // ═══════════════════════════════════════════

    public static class FrostFactory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;
        public FrostFactory(SpriteSet sprites) { this.sprites = sprites; }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xd, double yd, double zd, net.minecraft.util.RandomSource random) {
            var p = new SpellSnowflakeParticle(level, x, y, z, xd, yd, zd, sprites);
            p.setColor(Color.FROST.red(), Color.FROST.green(), Color.FROST.blue());
            p.alpha = 0.75F;
            return p;
        }
    }

    public static class HolyFactory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;
        public HolyFactory(SpriteSet sprites) { this.sprites = sprites; }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xd, double yd, double zd, net.minecraft.util.RandomSource random) {
            var p = new SpellSnowflakeParticle(level, x, y, z, xd, yd, zd, sprites);
            p.setColor(Color.HOLY.red(), Color.HOLY.green(), Color.HOLY.blue());
            p.alpha = 0.75F;
            return p;
        }
    }

    public static class DrippingBloodFactory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;
        public DrippingBloodFactory(SpriteSet sprites) { this.sprites = sprites; }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xd, double yd, double zd, net.minecraft.util.RandomSource random) {
            var p = new SpellSnowflakeParticle(level, x, y, z, xd, yd, zd, sprites);
            p.setColor(0.35F, 0, 0);
            p.alpha = 1F;
            p.glow = false;
            p.xd *= 0.4;
            p.zd *= 0.4;
            p.gravity = 0.8F;
            return p;
        }
    }

    public static class RootsFactory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;
        public RootsFactory(SpriteSet sprites) { this.sprites = sprites; }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xd, double yd, double zd, net.minecraft.util.RandomSource random) {
            var p = new SpellSnowflakeParticle(level, x, y, z, xd, yd, zd, sprites);
            p.setColor(1, 1, 1);
            p.alpha = 1F;
            p.glow = false;
            p.xd = 0;
            p.zd = 0;
            p.quadSize = 0.25F;
            return p;
        }
    }
}
