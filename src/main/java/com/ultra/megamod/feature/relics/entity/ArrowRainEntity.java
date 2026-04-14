package com.ultra.megamod.feature.relics.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class ArrowRainEntity extends Entity
{
	private int duration = 60;
	private int arrowsPerTick = 1;
	private int owner = -1;
	private float radius = 3.0F;
	private float damage = 2.0F;

	public ArrowRainEntity(EntityType<? extends ArrowRainEntity> type, Level level)
	{
		super(type, level);
	}

	public ArrowRainEntity(Level level, double x, double y, double z, int ownerId, int duration, float radius, float damage)
	{
		super(RelicEntityRegistry.ARROW_RAIN.get(), level);
		this.setPos(x, y, z);
		this.owner = ownerId;
		this.duration = duration;
		this.radius = radius;
		this.damage = damage;
	}

	@Override
	protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder)
	{
	}

	@Override
	public void tick()
	{
		super.tick();

		if(this.tickCount > duration)
		{
			this.discard();
			return;
		}

		if(!(level() instanceof ServerLevel server)) return;

		for(int i = 0; i < arrowsPerTick; i++)
		{
			double ox = (random.nextDouble() - 0.5) * 2 * radius;
			double oz = (random.nextDouble() - 0.5) * 2 * radius;
			Arrow arrow = new Arrow(server, getX() + ox, getY() + 12.0, getZ() + oz, ItemStack.EMPTY, ItemStack.EMPTY);
			arrow.setDeltaMovement(0, -1.2, 0);
			arrow.setBaseDamage(damage);
			arrow.pickup = AbstractArrow.Pickup.DISALLOWED;
			Entity ownerEntity = server.getEntity(owner);
			if(ownerEntity != null) arrow.setOwner(ownerEntity);
			server.addFreshEntity(arrow);
		}
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input)
	{
		duration = input.getIntOr("Duration", 60);
		radius = input.getFloatOr("Radius", 3.0F);
		damage = input.getFloatOr("Damage", 2.0F);
		owner = input.getIntOr("OwnerId", -1);
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output)
	{
		output.putInt("Duration", duration);
		output.putFloat("Radius", radius);
		output.putFloat("Damage", damage);
		output.putInt("OwnerId", owner);
	}

	@Override
	public boolean hurtServer(net.minecraft.server.level.ServerLevel level, net.minecraft.world.damagesource.DamageSource source, float amount)
	{
		return false;
	}
}
