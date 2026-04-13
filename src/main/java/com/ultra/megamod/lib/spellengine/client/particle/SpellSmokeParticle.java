package com.ultra.megamod.lib.spellengine.client.particle;



import net.minecraft.client.particle.*;
import net.minecraft.client.multiplayer.ClientLevel;

public class SpellSmokeParticle extends SingleQuadParticle {
	private SpriteSet sprites;
	SpellSmokeParticle(ClientLevel world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, boolean signal, SpriteSet sprites) {
		super(world, x, y, z, sprites.get(world.random));
		this.sprites = sprites;
		this.scale(3.0F);
		this.setSize(0.25F, 0.25F);
		if (signal) {
			this.lifetime = this.random.nextInt(50) + 280;
		} else {
			this.lifetime = this.random.nextInt(50) + 80;
		}

		this.gravity = 3.0E-6F;
		this.xd = velocityX;
		this.yd = velocityY + (double)(this.random.nextFloat() / 500.0F);
		this.zd = velocityZ;
	}

	@Override
	public void tick() {
		this.xo = this.x;
		this.yo = this.y;
		this.zo = this.z;
		if (this.age++ < this.lifetime && !(this.alpha <= 0.0F)) {
			this.xd = this.xd + (double)(this.random.nextFloat() / 5000.0F * (float)(this.random.nextBoolean() ? 1 : -1));
			this.zd = this.zd + (double)(this.random.nextFloat() / 5000.0F * (float)(this.random.nextBoolean() ? 1 : -1));
			this.yd = this.yd - (double)this.gravity;
			this.move(this.xd, this.yd, this.zd);
			if (this.age >= this.lifetime - 60 && this.alpha > 0.01F) {
				this.alpha -= 0.015F;
			}
		} else {
			this.remove();
		}
	}

	@Override
	public SingleQuadParticle.Layer getLayer() {
		return SingleQuadParticle.Layer.TRANSLUCENT;
	}

	public static class CosySmokeFactory implements ParticleProvider<TemplateParticleType> {
		private final SpriteSet spriteProvider;

		public CosySmokeFactory(SpriteSet spriteProvider) {
			this.spriteProvider = spriteProvider;
		}

		public Particle createParticle(TemplateParticleType templateParticleType, ClientLevel clientWorld, double d, double e, double f, double g, double h, double i, net.minecraft.util.RandomSource random) {
			SpellSmokeParticle particle = new SpellSmokeParticle(clientWorld, d, e, f, g, h, i, false, this.spriteProvider);

			TemplateParticleType.apply(templateParticleType, particle);

			particle.alpha *= 0.9F;
			return particle;
		}
	}
}
