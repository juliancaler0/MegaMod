package com.ultra.megamod.feature.combat.spell.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
/**
 * Long-lived smoke/signal particle with alpha fade.
 * Ported 1:1 from SpellEngine (net.spell_engine.client.particle.SpellSmokeParticle).
 */
public class SpellSmokeParticle extends SingleQuadParticle {

    SpellSmokeParticle(ClientLevel level, SpriteSet sprites, double x, double y, double z,
                       double xd, double yd, double zd, boolean signal) {
        super(level, x, y, z, sprites.get(level.random));
        this.scale(3.0F);
        if (signal) {
            this.lifetime = this.random.nextInt(50) + 280;
        } else {
            this.lifetime = this.random.nextInt(50) + 80;
        }
        this.gravity = 3.0E-6F;
        this.xd = xd;
        this.yd = yd + (this.random.nextFloat() / 500.0F);
        this.zd = zd;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ < this.lifetime && !(this.alpha <= 0.0F)) {
            this.xd += this.random.nextFloat() / 5000.0F * (this.random.nextBoolean() ? 1 : -1);
            this.zd += this.random.nextFloat() / 5000.0F * (this.random.nextBoolean() ? 1 : -1);
            this.yd -= this.gravity;
            this.move(this.xd, this.yd, this.zd);
            if (this.age >= this.lifetime - 60 && this.alpha > 0.01F) {
                this.alpha -= 0.015F;
            }
        } else {
            this.remove();
        }
    }

    @Override
    public net.minecraft.client.particle.SingleQuadParticle.Layer getLayer() {
        return net.minecraft.client.particle.SingleQuadParticle.Layer.TRANSLUCENT;
    }

    public static class CosySmokeFactory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;
        public CosySmokeFactory(SpriteSet sprites) { this.sprites = sprites; }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                        double xd, double yd, double zd, net.minecraft.util.RandomSource random) {
            var p = new SpellSmokeParticle(level, this.sprites, x, y, z, xd, yd, zd, false);

            TemplateParticleType.Appearance appearance = TemplateParticleType.consumeAppearance();
            TemplateParticleType.apply(appearance, p);

            p.alpha *= 0.9F;
            
            return p;
        }
    }
}
