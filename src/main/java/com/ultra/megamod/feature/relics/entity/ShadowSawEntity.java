package com.ultra.megamod.feature.relics.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public class ShadowSawEntity extends ThrowableProjectile
{
	private float damage = 6.0F;
	private boolean isReturning = false;

	public ShadowSawEntity(EntityType<? extends ShadowSawEntity> type, Level level)
	{
		super(type, level);
	}

	public void setDamage(float damage)
	{
		this.damage = damage;
	}

	@Override
	protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder)
	{
	}

	@Override
	public void tick()
	{
		this.move(MoverType.SELF, this.getDeltaMovement());
		if(level().isClientSide()) return;

		if(this.tickCount >= 60) isReturning = true;

		if(isReturning && this.getOwner() instanceof LivingEntity owner)
		{
			Vec3 toOwner = owner.getEyePosition().subtract(this.position());
			if(toOwner.lengthSqr() < 1.0)
			{
				this.discard();
				return;
			}
			this.setDeltaMovement(toOwner.normalize().scale(0.9));
		}

		if(!(level() instanceof ServerLevel server)) return;

		for(LivingEntity entity : level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(0.5)))
		{
			if(entity == this.getOwner()) continue;
			if(!entity.isAlive()) continue;

			if(this.getOwner() instanceof Player player)
			{
				entity.hurt(server.damageSources().playerAttack(player), damage);
			}
			else
			{
				entity.hurt(server.damageSources().magic(), damage);
			}
		}
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
		output.putFloat("Damage", damage);
		output.putBoolean("Returning", isReturning);
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input)
	{
		super.readAdditionalSaveData(input);
		damage = input.getFloatOr("Damage", 6.0F);
		isReturning = input.getBooleanOr("Returning", false);
	}
}
