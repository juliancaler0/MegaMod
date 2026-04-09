package com.ultra.megamod.feature.combat.spell.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
/**
 * Always-lit explosion particle for spell impact effects.
 * Ported 1:1 from SpellEngine (net.spell_engine.client.particle.SpellExplosionParticle).
 */
public class SpellExplosionParticle extends SingleQuadParticle {
    private final SpriteSet sprites;

    protected SpellExplosionParticle(ClientLevel level, double x, double y, double z,
                                     double xd, SpriteSet sprites) {
        super(level, x, y, z, sprites.get(level.random));
        this.sprites = sprites;
        this.lifetime = 6 + this.random.nextInt(4);
        float f = this.random.nextFloat() * 0.6F + 0.4F;
        this.rCol = f; this.gCol = f; this.bCol = f;
        this.quadSize = 2.0F * (1.0F - (float) xd * 0.5F);
        this.setSpriteFromAge(sprites);
    }

    @Override
    public int getLightColor(float tint) {
        return 0xF000F0; // Always fully lit
    }

    @Override
    public void tick() {
        this.xo = this.x; this.yo = this.y; this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
        } else {
            this.setSpriteFromAge(this.sprites);
        }
    }

    @Override
    public net.minecraft.client.particle.SingleQuadParticle.Layer getLayer() {
        return net.minecraft.client.particle.SingleQuadParticle.Layer.TRANSLUCENT;
    }

    public static class Factory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;
        public Factory(SpriteSet sprites) { this.sprites = sprites; }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                        double xd, double yd, double zd, net.minecraft.util.RandomSource random) {
            var p = new SpellExplosionParticle(level, x, y, z, xd, sprites);
            p.quadSize = 1.2F;
            p.rCol = 1F; p.gCol = 1F; p.bCol = 1F;
            p.lifetime = 10;
            return p;
        }
    }

    public static class TemplateFactory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;
        public TemplateFactory(SpriteSet sprites) { this.sprites = sprites; }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                        double xd, double yd, double zd, net.minecraft.util.RandomSource random) {
            var p = new SpellExplosionParticle(level, x, y, z, xd, sprites);
            p.quadSize = 1.2F;
            p.rCol = 1F; p.gCol = 1F; p.bCol = 1F;
            p.lifetime = 10;

            TemplateParticleType.Appearance appearance = TemplateParticleType.consumeAppearance();
            if (appearance != null) {
                TemplateParticleType.apply(appearance, p);
                if (appearance.color != null) p.alpha *= appearance.color.alpha();
                p.quadSize *= appearance.scale;
            }
            return p;
        }
    }
}
