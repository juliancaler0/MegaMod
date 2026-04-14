package com.ultra.megamod.feature.relics.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class StalactiteEntity extends ThrowableProjectile
{
	private float damage = 8.0F;

	public StalactiteEntity(EntityType<? extends StalactiteEntity> type, Level level)
	{
		super(type, level);
	}

	public void setDamage(float damage) { this.damage = damage; }

	@Override
	protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder)
	{
	}

	@Override
	public void tick()
	{
		this.move(MoverType.SELF, this.getDeltaMovement());
		if(this.tickCount > 200)
		{
			this.discard();
			return;
		}
		if(level().isClientSide()) return;

		if(onGround() && level() instanceof ServerLevel server)
		{
			for(LivingEntity entity : level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(1.5)))
			{
				if(entity == this.getOwner()) continue;
				entity.hurt(server.damageSources().fallingStalactite(this), damage);
			}
			this.discard();
		}
	}

	@Override
	protected double getDefaultGravity()
	{
		return 0.08;
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output)
	{
		super.addAdditionalSaveData(output);
		output.putFloat("Damage", damage);
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input)
	{
		super.readAdditionalSaveData(input);
		damage = input.getFloatOr("Damage", 8.0F);
	}
}
