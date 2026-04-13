package com.ultra.megamod.lib.spellengine.client.particle;



import net.minecraft.client.particle.*;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;
import com.ultra.megamod.lib.spellengine.client.util.Color;
import com.ultra.megamod.lib.spellpower.api.SpellSchool;
import com.ultra.megamod.lib.spellpower.api.SpellSchools;

public class SpellSnowflakeParticle extends SnowflakeParticle {
    boolean glow = true;
    protected SpellSnowflakeParticle(ClientLevel world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, SpriteSet spriteProvider) {
        super(world, x, y, z, velocityX, velocityY, velocityZ, spriteProvider);
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        return SingleQuadParticle.Layer.TRANSLUCENT;
    }

    public int getLightColor(float tint) {
        if (glow) {
            return 255;
        } else {
            return super.getLightColor(tint);
        }
    }

    public static class FrostFactory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteProvider;

        public FrostFactory(SpriteSet spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        public static Color color = Color.from(SpellSchools.FROST.color);

        @Override
        public Particle createParticle(SimpleParticleType SimpleParticleType, ClientLevel clientWorld, double d, double e, double f, double g, double h, double i, net.minecraft.util.RandomSource random) {
            var particle = new SpellSnowflakeParticle(clientWorld, d, e, f, g, h, i, this.spriteProvider);
            particle.setColor(color.red(), color.green(), color.red());
            particle.alpha = 0.75F;
            return particle;
        }
    }

    public static class HolyFactory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteProvider;

        public HolyFactory(SpriteSet spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        public static Color color = Color.from(0xffffcc);

        @Override
        public Particle createParticle(SimpleParticleType SimpleParticleType, ClientLevel clientWorld, double d, double e, double f, double g, double h, double i, net.minecraft.util.RandomSource random) {
            var particle = new SpellSnowflakeParticle(clientWorld, d, e, f, g, h, i, this.spriteProvider);
            particle.setColor(color.red(), color.green(), color.red());
            particle.alpha = 0.75F;
            return particle;
        }
    }

    public static class DrippingBloodFactory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteProvider;

        public DrippingBloodFactory(SpriteSet spriteProvider) {
            this.spriteProvider = spriteProvider;
        }
        public static Color color = Color.from(0xb30000);

        @Override
        public Particle createParticle(SimpleParticleType SimpleParticleType, ClientLevel clientWorld, double d, double e, double f, double g, double h, double i, net.minecraft.util.RandomSource random) {
            var particle = new SpellSnowflakeParticle(clientWorld, d, e, f, g, h, i, this.spriteProvider);
            particle.setColor(0.35F, 0, 0);
            particle.alpha = 1F;
            particle.glow = false;
            particle.xd *= 0.4;
            particle.zd *= 0.4;
            particle.gravity = 0.8F;
            return particle;
        }
    }

    public static class RootsFactory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteProvider;

        public RootsFactory(SpriteSet spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        public static Color color = Color.from(SpellSchools.FROST.color);

        @Override
        public Particle createParticle(SimpleParticleType SimpleParticleType, ClientLevel clientWorld, double d, double e, double f, double g, double h, double i, net.minecraft.util.RandomSource random) {
            var particle = new SpellSnowflakeParticle(clientWorld, d, e, f, g, h, i, this.spriteProvider);
            particle.setColor(1, 1, 1);
            particle.alpha = 1F;
            particle.glow = false;
            particle.setSize(3f, 3f);
            particle.xd = 0;
            particle.zd = 0;
            particle.quadSize = 0.25F;
            return particle;
        }
    }
}