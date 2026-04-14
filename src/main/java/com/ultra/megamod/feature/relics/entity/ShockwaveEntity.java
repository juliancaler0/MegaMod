package com.ultra.megamod.feature.relics.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public class ShockwaveEntity extends Entity
{
	private float damage = 6.0F;
	private float maxRadius = 6.0F;
	private float currentRadius = 0.0F;
	private int owner = -1;

	public ShockwaveEntity(EntityType<? extends ShockwaveEntity> type, Level level)
	{
		super(type, level);
	}

	public ShockwaveEntity(Level level, double x, double y, double z, int ownerId, float damage, float maxRadius)
	{
		super(RelicEntityRegistry.SHOCKWAVE.get(), level);
		this.setPos(x, y, z);
		this.owner = ownerId;
		this.damage = damage;
		this.maxRadius = maxRadius;
	}

	@Override
	protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder)
	{
	}

	@Override
	public void tick()
	{
		super.tick();

		currentRadius += 0.5F;

		if(currentRadius > maxRadius)
		{
			this.discard();
			return;
		}

		if(level().isClientSide())
		{
			for(int i = 0; i < 32; i++)
			{
				double angle = (i / 32.0) * Math.PI * 2;
				double px = getX() + Math.cos(angle) * currentRadius;
				double pz = getZ() + Math.sin(angle) * currentRadius;
				level().addParticle(ParticleTypes.POOF, px, getY() + 0.1, pz, 0, 0.05, 0);
			}
			return;
		}

		if(!(level() instanceof ServerLevel server)) return;

		for(LivingEntity entity : level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(currentRadius, 2.0, currentRadius)))
		{
			if(entity.getId() == owner) continue;
			if(!entity.isAlive()) continue;

			double dist = Math.sqrt(Math.pow(entity.getX() - getX(), 2) + Math.pow(entity.getZ() - getZ(), 2));
			if(dist > currentRadius - 0.5 && dist < currentRadius + 0.5)
			{
				entity.hurt(server.damageSources().magic(), damage);
				Vec3 push = new Vec3(entity.getX() - getX(), 0.3, entity.getZ() - getZ()).normalize().scale(0.8);
				entity.push(push.x, push.y, push.z);
			}
		}
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input)
	{
		damage = input.getFloatOr("Damage", 6.0F);
		maxRadius = input.getFloatOr("MaxRadius", 6.0F);
		owner = input.getIntOr("OwnerId", -1);
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output)
	{
		output.putFloat("Damage", damage);
		output.putFloat("MaxRadius", maxRadius);
		output.putInt("OwnerId", owner);
	}

	@Override
	public boolean hurtServer(net.minecraft.server.level.ServerLevel level, net.minecraft.world.damagesource.DamageSource source, float amount)
	{
		return false;
	}
}
