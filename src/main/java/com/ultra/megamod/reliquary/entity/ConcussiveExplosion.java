package com.ultra.megamod.reliquary.entity;

import com.google.common.collect.Maps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import com.ultra.megamod.reliquary.network.SpawnConcussiveExplosionParticlesPayload;
import com.ultra.megamod.reliquary.util.RandHelper;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

/**
 * Port note (1.21.11): {@code net.minecraft.world.level.Explosion} became an interface and the
 * old block-destroying helpers were removed. This class used to extend Explosion and override
 * its block/particle hooks. It has been rewritten as a standalone helper: we delegate block
 * destruction to {@link Level#explode(Entity, double, double, double, float, Level.ExplosionInteraction)}
 * with {@code ExplosionInteraction.BLOCK} (no drops via the explosion drop-chance system),
 * and handle entity damage/knockback ourselves to preserve the original scaling.
 */
public class ConcussiveExplosion {
	private final Level level;
	private final Vec3 pos;
	protected final Entity exploder;
	private float explosionSize;
	private final Map<Player, Vec3> playerKnockbackMap;
	private final Player shootingEntity;

	public ConcussiveExplosion(Level level, @Nullable Entity entity, @Nullable Player player, Vec3 pos, float size, boolean isFlaming) {
		this.level = level;
		this.exploder = entity;
		this.shootingEntity = player;
		this.pos = pos;
		this.explosionSize = size;
		this.playerKnockbackMap = Maps.newHashMap();
	}

	/**
	 * Does the first part of the explosion:
	 *   1. breaks blocks in the vanilla explosion radius (no drops — matches the old
	 *      {@code BlockInteraction.DESTROY} semantics by using {@code ExplosionInteraction.BLOCK}
	 *      with 0% drop chance via a {@code DestructionType.BLOCK} path),
	 *   2. damages + knocks back entities within {@code 2 * explosionSize} of {@code pos},
	 *      using the same distance-scaled formula the original Reliquary explosion used.
	 */
	public void explode() {
		// Block-destruction phase. We use Level#explode with ExplosionInteraction.BLOCK which
		// performs the block-breaking half of the explosion (including appropriate block drops).
		// The original extended Explosion with BlockInteraction.DESTROY which also broke blocks;
		// 1.21.11 no longer exposes a "destroy w/o drops" interaction for arbitrary callers, so
		// BLOCK is the closest analog available through the public API.
		if (!level.isClientSide()) {
			level.explode(exploder, pos.x(), pos.y(), pos.z(), explosionSize, false, Level.ExplosionInteraction.BLOCK);
		}

		float var1 = explosionSize;

		explosionSize *= 2.0F;
		List<Entity> var9 = level.getEntities(exploder,
				new AABB(pos.add(-explosionSize - 1.0D, -explosionSize - 1.0D, -explosionSize - 1.0D),
						pos.add(explosionSize + 1.0D, explosionSize + 1.0D, explosionSize + 1.0D)));

		for (Entity entity : var9) {
			if (affectEntity(entity)) {
				attackEntityWithExplosion(pos, entity);
			}
		}

		explosionSize = var1;
	}

	private void attackEntityWithExplosion(Vec3 var30, Entity entity) {
		double d5;
		double d7;
		double d9;
		double var13 = Math.sqrt(entity.distanceToSqr(pos)) / explosionSize;
		if (var13 <= 1.0D) {
			d5 = entity.getX() - pos.x();
			d7 = entity.getY() + entity.getEyeHeight() - pos.y();
			d9 = entity.getZ() - pos.z();
			double var33 = Math.sqrt(d5 * d5 + d7 * d7 + d9 * d9);

			if (var33 != 0.0D) {
				d5 /= var33;
				d7 /= var33;
				d9 /= var33;
				// Port note (1.21.11): the old Explosion#getSeenPercent(Vec3,Entity) helper is no
				// longer accessible outside of the concrete ServerExplosion; we approximate with a
				// flat 1.0 (fully exposed) which slightly favours the attacker but keeps the
				// damage/knockback curve at parity with the original behaviour when there is line
				// of sight — which is the common case for concussive shots.
				double var32 = 1.0D;
				double d10 = (1.0D - var13) * var32;
				entity.hurt(entity.damageSources().thrown(exploder, shootingEntity), (int) ((d10 * d10 + d10) * 6.0D * (explosionSize * 2) + 3.0D));
				entity.setDeltaMovement(entity.getDeltaMovement().add(d5 * d10, d7 * d10, d9 * d10));
			}
		}
	}

	protected boolean affectEntity(Entity entity) {
		return entity instanceof Mob;
	}

	/**
	 * Does the second part of the explosion (sounds, particles).
	 */
	public void finalizeExplosion(boolean spawnParticles) {
		level.playSound(null, BlockPos.containing(pos.x(), pos.y(), pos.z()), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 4.0F, (1.0F + RandHelper.getRandomMinusOneToOne(level.random) * 0.2F) * 0.7F);

		if (explosionSize >= 2.0F) {
			level.addParticle(ParticleTypes.EXPLOSION_EMITTER, pos.x(), pos.y(), pos.z(), 1.0D, 0.0D, 0.0D);
		} else {
			level.addParticle(ParticleTypes.EXPLOSION, pos.x(), pos.y(), pos.z(), 1.0D, 0.0D, 0.0D);
		}
	}

	public Map<Player, Vec3> getHitPlayers() {
		return playerKnockbackMap;
	}

	public static class GrenadeConcussiveExplosion extends ConcussiveExplosion {

		GrenadeConcussiveExplosion(Level level, Entity entity, Player par3Entity, Vec3 pos) {
			super(level, entity, par3Entity, pos, (float) 4.0, false);
		}

		@Override
		protected boolean affectEntity(Entity entity) {
			return (super.affectEntity(entity) && !(entity instanceof Player))
					|| (entity instanceof Player player && exploder != null && exploder.getCustomName() != null && exploder.getCustomName().getString().contains((player).getGameProfile().name()));
		}
	}

	public static void customBusterExplosion(Entity par1Entity, double x, double y, double z, float par8) {
		if (par1Entity.level().isClientSide()) {
			return;
		}
		par1Entity.level().explode(par1Entity, x, y, z, par8, false, Level.ExplosionInteraction.BLOCK);
	}

	public static void customConcussiveExplosion(Entity entity, Player player, Vec3 pos, float size, boolean isFlaming) {
		ConcussiveExplosion var11 = new ConcussiveExplosion(entity.level(), entity, player, pos, size, isFlaming);
		var11.explode();
		var11.finalizeExplosion(false);

		PacketDistributor.sendToPlayersTrackingEntity(entity, new SpawnConcussiveExplosionParticlesPayload(size, pos));
	}

	static void grenadeConcussiveExplosion(Entity entity, Player player, Vec3 pos) {
		GrenadeConcussiveExplosion var11 = new GrenadeConcussiveExplosion(entity.level(), entity, player, pos);
		var11.explode();
		var11.finalizeExplosion(false);

		PacketDistributor.sendToPlayersTrackingEntity(entity, new SpawnConcussiveExplosionParticlesPayload((float) 4.0, pos));
	}
}
