package com.ultra.megamod.feature.relics.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

public class ShadowGlaiveEntity extends ThrowableProjectile
{
	private static final EntityDataAccessor<Integer> BOUNCES_LEFT = SynchedEntityData.defineId(ShadowGlaiveEntity.class, EntityDataSerializers.INT);

	private float damage = 4.0F;
	private float searchRadius = 6.0F;
	private LivingEntity currentTarget;
	private final Set<Integer> hitIds = new HashSet<>();

	public ShadowGlaiveEntity(EntityType<? extends ShadowGlaiveEntity> type, Level level)
	{
		super(type, level);
	}

	public void setParams(float damage, int maxBounces, float searchRadius)
	{
		this.damage = damage;
		this.searchRadius = searchRadius;
		this.entityData.set(BOUNCES_LEFT, maxBounces);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder)
	{
		builder.define(BOUNCES_LEFT, 0);
	}

	@Override
	public void tick()
	{
		super.tick();

		if(this.tickCount > 200 || this.entityData.get(BOUNCES_LEFT) <= 0)
		{
			this.discard();
			return;
		}

		if(level().isClientSide()) return;

		if(currentTarget == null || !currentTarget.isAlive())
		{
			currentTarget = findNextTarget();
			if(currentTarget == null)
			{
				this.discard();
				return;
			}
		}

		Vec3 diff = currentTarget.position().add(0, currentTarget.getBbHeight() * 0.5, 0).subtract(this.position());
		this.setDeltaMovement(diff.normalize().scale(0.8));

		AABB hitBox = this.getBoundingBox().inflate(0.4);
		if(hitBox.intersects(currentTarget.getBoundingBox()))
		{
			hitTarget(currentTarget);
			hitIds.add(currentTarget.getId());
			entityData.set(BOUNCES_LEFT, entityData.get(BOUNCES_LEFT) - 1);
			currentTarget = null;
		}
	}

	private void hitTarget(LivingEntity target)
	{
		if(!(level() instanceof ServerLevel server)) return;

		Player owner = (this.getOwner() instanceof Player p) ? p : null;
		if(owner != null)
		{
			target.hurt(server.damageSources().playerAttack(owner), damage);
		}
		else
		{
			target.hurt(server.damageSources().magic(), damage);
		}
	}

	private LivingEntity findNextTarget()
	{
		AABB search = this.getBoundingBox().inflate(searchRadius);
		return level().getEntitiesOfClass(LivingEntity.class, search).stream()
			.filter(e -> e.isAlive() && e != this.getOwner() && !hitIds.contains(e.getId()))
			.min(Comparator.comparingDouble(e -> e.distanceToSqr(this)))
			.orElse(null);
	}

	@Override
	protected double getDefaultGravity()
	{
		return 0.0;
	}

	@Override
	public boolean isPushedByFluid()
	{
		return false;
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output)
	{
		super.addAdditionalSaveData(output);
		output.putInt("BouncesLeft", entityData.get(BOUNCES_LEFT));
		output.putFloat("Damage", damage);
		output.putFloat("SearchRadius", searchRadius);
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input)
	{
		super.readAdditionalSaveData(input);
		entityData.set(BOUNCES_LEFT, input.getIntOr("BouncesLeft", 0));
		damage = input.getFloatOr("Damage", 4.0F);
		searchRadius = input.getFloatOr("SearchRadius", 6.0F);
	}
}
