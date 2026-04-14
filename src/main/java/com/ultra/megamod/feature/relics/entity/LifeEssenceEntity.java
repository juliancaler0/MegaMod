package com.ultra.megamod.feature.relics.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public class LifeEssenceEntity extends Entity
{
	private int owner = -1;
	private float heal = 2.0F;

	public LifeEssenceEntity(EntityType<? extends LifeEssenceEntity> type, Level level)
	{
		super(type, level);
	}

	public LifeEssenceEntity(Level level, double x, double y, double z, int ownerId, float heal)
	{
		super(RelicEntityRegistry.LIFE_ESSENCE.get(), level);
		this.setPos(x, y, z);
		this.owner = ownerId;
		this.heal = heal;
	}

	@Override
	protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder)
	{
	}

	@Override
	public void tick()
	{
		super.tick();

		if(this.tickCount > 200)
		{
			this.discard();
			return;
		}

		if(level().isClientSide()) return;

		Entity ownerEntity = level().getEntity(owner);
		if(ownerEntity instanceof Player player && player.isAlive())
		{
			Vec3 diff = player.position().add(0, player.getBbHeight() * 0.5, 0).subtract(this.position());
			double dist = diff.length();

			if(dist < 1.0)
			{
				player.heal(heal);
				this.discard();
				return;
			}

			this.setDeltaMovement(diff.normalize().scale(0.3));
			this.move(net.minecraft.world.entity.MoverType.SELF, this.getDeltaMovement());
		}
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input)
	{
		owner = input.getIntOr("OwnerId", -1);
		heal = input.getFloatOr("Heal", 2.0F);
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output)
	{
		output.putInt("OwnerId", owner);
		output.putFloat("Heal", heal);
	}

	@Override
	public boolean hurtServer(net.minecraft.server.level.ServerLevel level, net.minecraft.world.damagesource.DamageSource source, float amount)
	{
		return false;
	}
}
