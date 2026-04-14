package com.ultra.megamod.reliquary.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

import com.ultra.megamod.reliquary.api.IPedestal;
import com.ultra.megamod.reliquary.client.render.PedestalFishHookRenderer;
import com.ultra.megamod.reliquary.init.ModItems;
import com.ultra.megamod.reliquary.network.PedestalFishHookPayload;
import com.ultra.megamod.reliquary.network.SpawnAngelheartVialParticlesPayload;
import com.ultra.megamod.reliquary.network.SpawnPhoenixDownParticlesPayload;
import com.ultra.megamod.reliquary.network.SpawnThrownPotionImpactParticlesPayload;
import com.ultra.megamod.reliquary.util.WorldHelper;

/**
 * Client-only proxy that installs the real handlers onto the
 * {@code CLIENT_HANDLER} hooks of Reliquary's S2C payloads. Called from
 * {@link com.ultra.megamod.reliquary.Reliquary#initClient} so neither this
 * class nor its {@code net.minecraft.client.*} references are ever loaded on
 * the dedicated server (which would be rejected by
 * {@code NeoForgeDevDistCleaner} during payload registration).
 */
public final class ReliquaryClientPayloadProxy {
	private ReliquaryClientPayloadProxy() {}

	public static void init() {
		PedestalFishHookPayload.CLIENT_HANDLER = ReliquaryClientPayloadProxy::onPedestalFishHook;
		SpawnThrownPotionImpactParticlesPayload.CLIENT_HANDLER = ReliquaryClientPayloadProxy::onThrownPotionImpactParticles;
		SpawnPhoenixDownParticlesPayload.CLIENT_HANDLER = ReliquaryClientPayloadProxy::onPhoenixDownParticles;
		SpawnAngelheartVialParticlesPayload.CLIENT_HANDLER = ReliquaryClientPayloadProxy::onAngelheartVialParticles;
	}

	private static void onPedestalFishHook(PedestalFishHookPayload payload) {
		ClientLevel level = Minecraft.getInstance().level;
		WorldHelper.getBlockEntity(level, payload.pedestalPos(), IPedestal.class).ifPresent(pedestal -> {
			PedestalFishHookRenderer.HookRenderingData data = null;
			if (payload.hookY() > 0) {
				data = new PedestalFishHookRenderer.HookRenderingData(payload.hookX(), payload.hookY(), payload.hookZ());
			}
			pedestal.setItemData(data);
		});
	}

	private static void onThrownPotionImpactParticles(SpawnThrownPotionImpactParticlesPayload payload) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null) {
			return;
		}

		RandomSource rand = mc.level.random;

		float red = (((payload.color() >> 16) & 255) / 256F);
		float green = (((payload.color() >> 8) & 255) / 256F);
		float blue = ((payload.color() & 255) / 256F);

		for (int i = 0; i < 100; ++i) {
			double var39 = rand.nextDouble() * 4.0D;
			double angle = rand.nextDouble() * Math.PI * 2.0D;
			double xSpeed = Math.cos(angle) * var39;
			double ySpeed = 0.01D + rand.nextDouble() * 0.5D;
			double zSpeed = Math.sin(angle) * var39;

			// Port note (1.21.11): Particle#setColor(float,float,float) was removed. We construct
			// a ColorParticleOption over ENTITY_EFFECT that carries an ARGB int; the renderer
			// picks up the tint from the options object itself. The original per-particle
			// "0.75 + 0.25 * rand" brightness multiplier is preserved via the argb pack below,
			// so the splash colour matches the thrown-potion's declared tint colour as before.
			float var32 = 0.75F + rand.nextFloat() * 0.25F;
			int argb = 0xFF000000
					| (((int) Math.min(255, red * var32 * 255)) << 16)
					| (((int) Math.min(255, green * var32 * 255)) << 8)
					| ((int) Math.min(255, blue * var32 * 255));
			Particle particle = mc.particleEngine.createParticle(
					ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, argb),
					payload.posX() + xSpeed * 0.1D, payload.posY() + 0.3D, payload.posZ() + zSpeed * 0.1D, xSpeed, ySpeed, zSpeed);
			if (particle != null) {
				particle.setPower((float) var39);
			}
		}
	}

	private static void onPhoenixDownParticles(SpawnPhoenixDownParticlesPayload payload) {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null) {
			return;
		}

		for (int particles = 0; particles <= 400; particles++) {
			RandomSource random = player.level().random;
			player.level().addParticle(ParticleTypes.FLAME, payload.position().x, payload.position().y, payload.position().z, random.nextGaussian() * 8, random.nextGaussian() * 8, random.nextGaussian() * 8);
		}
	}

	private static void onAngelheartVialParticles(SpawnAngelheartVialParticlesPayload payload) {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null) {
			return;
		}

		double x = payload.position().x;
		double y = payload.position().y;
		double z = payload.position().z;
		RandomSource random = player.level().random;
		ItemParticleOption itemParticleData = new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(ModItems.ANGELHEART_VIAL.get()));
		for (int i = 0; i < 8; ++i) {
			player.level().addParticle(itemParticleData, x, y, z, random.nextGaussian() * 0.15D, random.nextDouble() * 0.2D, random.nextGaussian() * 0.15D);
		}

		float red = 1.0F;
		float green = 0.0F;
		float blue = 1.0F;

		for (int i = 0; i < 100; ++i) {
			double distance = random.nextDouble() * 4.0D;
			double angle = random.nextDouble() * Math.PI * 2.0D;
			double xSpeed = Math.cos(angle) * distance;
			double ySpeed = 0.01D + random.nextDouble() * 0.5D;
			double zSpeed = Math.sin(angle) * distance;
			// Port note (1.21.11): the old Particle#setColor(float,float,float) sink was removed,
			// and ParticleTypes.EFFECT switched to a typed SpellParticleOption. We instead spawn
			// a ColorParticleOption over ENTITY_EFFECT which carries an ARGB int and renders at
			// the same "wispy cloud" visual as the old ParticleTypes.EFFECT. The original red=1,
			// green=0, blue=1 (magenta) tint is preserved via the argb pack below.
			float colorMultiplier = 0.75F + random.nextFloat() * 0.25F;
			int argb = 0xFF000000
					| (((int) Math.min(255, red * colorMultiplier * 255)) << 16)
					| (((int) Math.min(255, green * colorMultiplier * 255)) << 8)
					| ((int) Math.min(255, blue * colorMultiplier * 255));
			Particle particle = Minecraft.getInstance().particleEngine.createParticle(
					ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, argb),
					x + xSpeed * 0.1D, y + 0.3D, z + zSpeed * 0.1D, xSpeed, ySpeed, zSpeed);
			if (particle != null) {
				particle.setPower((float) distance);
			}
		}
	}
}
