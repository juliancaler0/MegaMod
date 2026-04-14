package com.ultra.megamod.feature.relics.entity;

import com.ultra.megamod.feature.relics.effect.RelicEffectRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class SporeEntity extends Entity
{
	private float damage = 1.0F;
	private int lifespan = 100;
	private int owner = -1;

	public SporeEntity(EntityType<? extends SporeEntity> type, Level level)
	{
		super(type, level);
	}

	public SporeEntity(Level level, double x, double y, double z, int ownerId, float damage, int lifespan)
	{
		super(RelicEntityRegistry.SPORE.get(), level);
		this.setPos(x, y, z);
		this.owner = ownerId;
		this.damage = damage;
		this.lifespan = lifespan;
	}

	@Override
	protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder)
	{
	}

	@Override
	public void tick()
	{
		super.tick();

		if(this.tickCount > lifespan)
		{
			this.discard();
			return;
		}

		if(level().isClientSide())
		{
			if(this.tickCount % 3 == 0)
			{
				level().addParticle(ParticleTypes.SPORE_BLOSSOM_AIR,
					getX() + (random.nextDouble() - 0.5) * 1.0,
					getY() + random.nextDouble() * 0.8,
					getZ() + (random.nextDouble() - 0.5) * 1.0,
					0.0, 0.02, 0.0);
			}
			return;
		}

		if(!(level() instanceof ServerLevel server)) return;

		if(this.tickCount % 20 == 0)
		{
			for(LivingEntity entity : level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(1.0)))
			{
				if(entity.getId() == owner) continue;
				if(!entity.isAlive()) continue;

				entity.hurt(server.damageSources().magic(), damage);
				entity.addEffect(new MobEffectInstance(RelicEffectRegistry.CONFUSION, 60, 0));
			}
		}
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input)
	{
		damage = input.getFloatOr("Damage", 1.0F);
		lifespan = input.getIntOr("Lifespan", 100);
		owner = input.getIntOr("OwnerId", -1);
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output)
	{
		output.putFloat("Damage", damage);
		output.putInt("Lifespan", lifespan);
		output.putInt("OwnerId", owner);
	}

	@Override
	public boolean hurtServer(net.minecraft.server.level.ServerLevel level, net.minecraft.world.damagesource.DamageSource source, float amount)
	{
		return false;
	}
}
