package com.ultra.megamod.feature.relics.entity;

import com.ultra.megamod.feature.relics.effect.RelicEffectRegistry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class DissectionEntity extends ThrowableProjectile
{
	private float damage = 6.0F;
	private int bleedDuration = 100;

	public DissectionEntity(EntityType<? extends DissectionEntity> type, Level level)
	{
		super(type, level);
	}

	public void setParams(float damage, int bleedDuration)
	{
		this.damage = damage;
		this.bleedDuration = bleedDuration;
	}

	@Override
	protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder)
	{
	}

	@Override
	public void tick()
	{
		this.move(MoverType.SELF, this.getDeltaMovement());

		if(this.tickCount > 40)
		{
			this.discard();
			return;
		}

		if(!(level() instanceof ServerLevel server)) return;

		for(LivingEntity entity : level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(0.3)))
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
			entity.addEffect(new MobEffectInstance(RelicEffectRegistry.BLEEDING, bleedDuration, 0));
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
		output.putInt("BleedDuration", bleedDuration);
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input)
	{
		super.readAdditionalSaveData(input);
		damage = input.getFloatOr("Damage", 6.0F);
		bleedDuration = input.getIntOr("BleedDuration", 100);
	}
}
