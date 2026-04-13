package com.ultra.megamod.lib.spellengine.client.particle;



import net.minecraft.client.particle.HugeExplosionParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;

public class SpellExplosionParticle extends HugeExplosionParticle {
    protected SpellExplosionParticle(ClientLevel world, double x, double y, double z, double d, SpriteSet spriteProvider) {
        super(world, x, y, z, d, spriteProvider);
    }

    public int getLightColor(float tint) {
        return 255;
    }

    public static class Factory implements ParticleProvider<SimpleParticleType> {

        private final SpriteSet spriteProvider;

        public Factory(SpriteSet spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        public Particle createParticle(SimpleParticleType SimpleParticleType, ClientLevel clientWorld, double d, double e, double f, double g, double h, double i, net.minecraft.util.RandomSource random) {
            var particle = new SpellExplosionParticle(clientWorld, d, e, f, g, this.spriteProvider);
            particle.quadSize = 1.2F;
            particle.rCol = 1F;
            particle.gCol = 1F;
            particle.bCol = 1F;
            particle.lifetime = 10;
            return particle;
        }
    }

    public static class TemplateFactory implements ParticleProvider<TemplateParticleType> {

        private final SpriteSet spriteProvider;

        public TemplateFactory(SpriteSet spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        public Particle createParticle(TemplateParticleType particleType, ClientLevel clientWorld, double d, double e, double f, double g, double h, double i, net.minecraft.util.RandomSource random) {
            var particle = new SpellExplosionParticle(clientWorld, d, e, f, g, this.spriteProvider);
            particle.quadSize = 1.2F;
            particle.rCol = 1F;
            particle.gCol = 1F;
            particle.bCol = 1F;
            particle.lifetime = 10;

            TemplateParticleType.apply(particleType, particle);
            var appearance = particleType.getAppearance();
            if (appearance != null) {
                var color = appearance.color;
                if (color != null) {
                    particle.alpha *= appearance.color.alpha();
                }
                particle.quadSize *= appearance.scale;
                // particle.followEntity = appearance.entityFollowed;
            }

            return particle;
        }
    }
}
