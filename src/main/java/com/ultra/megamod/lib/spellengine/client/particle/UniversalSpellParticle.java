package com.ultra.megamod.lib.spellengine.client.particle;



import net.minecraft.client.particle.*;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;
import com.ultra.megamod.lib.spellengine.fx.SpellEngineParticles;

public class UniversalSpellParticle extends SingleQuadParticle  {
    private static final RandomSource RANDOM = net.minecraft.util.RandomSource.create();
    private final SpriteSet spriteProvider;
    private final SpellEngineParticles.MagicParticleFamily.Motion motion;
    public boolean glows = true;
    public boolean translucent = true;

    UniversalSpellParticle(ClientLevel world, SpriteSet spriteProvider, SpellEngineParticles.MagicParticleFamily.Motion motion, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        super(world, x, y, z, 0.5 - RANDOM.nextDouble(), velocityY, 0.5 - RANDOM.nextDouble(), spriteProvider.get(world.random));
        this.spriteProvider = spriteProvider;
        this.motion = motion;

        switch (motion) {
            case FLOAT, DECELERATE -> {
                this.friction = 0.96F;
                this.xd = this.xd * 0.01F + velocityX;
                this.yd = this.yd * 0.01F + velocityY;
                this.zd = this.zd * 0.01F + velocityZ;
                this.x = this.x + (double)((this.random.nextFloat() - this.random.nextFloat()) * 0.05F);
                this.y = this.y + (double)((this.random.nextFloat() - this.random.nextFloat()) * 0.05F);
                this.z = this.z + (double)((this.random.nextFloat() - this.random.nextFloat()) * 0.05F);
                if (motion == SpellEngineParticles.MagicParticleFamily.Motion.DECELERATE) {
                    this.friction *= 0.8F;
                }
                this.lifetime = (int)(8.0 / (Math.random() * 0.8 + 0.2));
            }
            case ASCEND -> {
                this.friction = 0.96F;
                this.gravity = -0.1F;
                // ascending handled by negative gravity
                this.yd *= 0.2;
                if (velocityX == 0.0 && velocityZ == 0.0) {
                    this.xd *= 0.10000000149011612;
                    this.zd *= 0.10000000149011612;
                }
                this.lifetime = (int)(8.0 / (Math.random() * 0.8 + 0.2));
            }
            case BURST -> {
                this.friction = 0.7f;
                this.gravity = 0.5f;
                this.xd *= (double)0.1f;
                this.yd *= (double)0.1f;
                this.zd *= (double)0.1f;
                this.xd += velocityX * 0.4;
                this.yd += velocityY * 0.4;
                this.zd += velocityZ * 0.4;
                this.lifetime = Math.max((int)(6.0 / (Math.random() * 0.8 + 0.6)), 1);
            }
        }

        this.setSpriteFromAge(spriteProvider);
        this.hasPhysics = false;
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        if (translucent) {
            return SingleQuadParticle.Layer.TRANSLUCENT;
        } else if (glows) {
            return SingleQuadParticle.Layer.TRANSLUCENT;
        } else {
            return SingleQuadParticle.Layer.OPAQUE;
        }
    }

    public int getLightColor(float tint) {
        return glows ? 255 : super.getLightColor(tint);
    }

    // MARK: Factories

    public static class MagicVariant implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteProvider;
        private final SpellEngineParticles.MagicParticleFamily.Variant particleVariant;

        public MagicVariant(SpriteSet spriteProvider, SpellEngineParticles.MagicParticleFamily.Variant particleVariant) {
            this.spriteProvider = spriteProvider;
            this.particleVariant = particleVariant;
        }

        public Particle createParticle(SimpleParticleType SimpleParticleType, ClientLevel clientWorld, double d, double e, double f, double g, double h, double i, net.minecraft.util.RandomSource random) {
            var particle = new UniversalSpellParticle(clientWorld, this.spriteProvider, particleVariant.motion(), d, e, f, g, h, i);
            particle.glows = true;
            float j = clientWorld.random.nextFloat() * 0.5F + 0.35F;
            var color = particleVariant.color();
            particle.setColor(color.red() * j, color.green() * j, color.blue() * j);
            particle.quadSize *= 0.75f;

            switch (particleVariant.shape()) {
                case SPELL, STRIPE -> {
                    particle.alpha = 1F;
                }
                default -> {
                    particle.alpha = 0.75F;
                }
            }

            return particle;
        }
    }

    public static class Opaque implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteProvider;
        private final SpellEngineParticles.MagicParticleFamily.Motion motion;

        public Opaque(SpriteSet spriteProvider, SpellEngineParticles.MagicParticleFamily.Motion motion) {
            this.spriteProvider = spriteProvider;
            this.motion = motion;
        }

        public Particle createParticle(SimpleParticleType SimpleParticleType, ClientLevel clientWorld, double d, double e, double f, double g, double h, double i, net.minecraft.util.RandomSource random) {
            var particle = new UniversalSpellParticle(clientWorld, this.spriteProvider, motion, d, e, f, g, h, i);
            float j = clientWorld.random.nextFloat() * 0.25F + 0.7F;
            particle.setColor(j,j,j);
            return particle;
        }
    }
}
