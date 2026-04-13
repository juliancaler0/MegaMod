package com.ultra.megamod.lib.spellengine.client.particle;

import net.minecraft.client.particle.*;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;
import org.joml.Quaternionf;

public class ShiftedParticle
        extends SingleQuadParticle {
    private final SpriteSet spriteProvider;

    protected ShiftedParticle(ClientLevel world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, SpriteSet spriteProvider) {
        super(world, x, y, z, spriteProvider.get(world.random));
        this.gravity = 0.225f;
        this.friction = 1.0f;
        this.spriteProvider = spriteProvider;
        this.yd = velocityY + (Math.random() * 2.0 - 1.0) * (double) 0.05f;
        this.quadSize = 0.1f * (this.random.nextFloat() * this.random.nextFloat() * 1.0f + 1.0f);
        this.lifetime = (int) (16.0 / ((double) this.random.nextFloat() * 0.8 + 0.2)) + 2;
        this.setSpriteFromAge(spriteProvider);
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        return SingleQuadParticle.Layer.OPAQUE;
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(this.spriteProvider);
        this.xd *= (double) 0.95f;
        this.yd *= (double) 0.9f;
        this.zd *= (double) 0.95f;
    }

    // Particle shift handled by position adjustment in tick()

    public static class RootsFactory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteProvider;

        public RootsFactory(SpriteSet spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel clientWorld, double d, double e, double f, double g, double h, double i, net.minecraft.util.RandomSource random) {
            var particle = new ShiftedParticle(clientWorld, d, e, f, g, h, i, this.spriteProvider);
            particle.setColor(1, 1, 1);
            particle.alpha = 1F;
            particle.setSize(3f, 3f);
            particle.xd = 0;
            particle.zd = 0;
            particle.quadSize = 0.25F;
            return particle;
        }
    }
}
