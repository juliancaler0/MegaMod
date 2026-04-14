package com.ultra.megamod.feature.relics.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public class RelicExperienceOrbEntity extends Entity
{
	private int xpValue = 1;

	public RelicExperienceOrbEntity(EntityType<? extends RelicExperienceOrbEntity> type, Level level)
	{
		super(type, level);
	}

	public RelicExperienceOrbEntity(Level level, double x, double y, double z, int xpValue)
	{
		super(RelicEntityRegistry.RELIC_XP_ORB.get(), level);
		this.setPos(x, y, z);
		this.xpValue = xpValue;
	}

	@Override
	protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder)
	{
	}

	@Override
	public void tick()
	{
		super.tick();

		if(this.tickCount > 6000)
		{
			this.discard();
			return;
		}

		this.setDeltaMovement(this.getDeltaMovement().add(0, -0.03, 0));
		this.move(MoverType.SELF, this.getDeltaMovement());

		if(this.onGround())
		{
			this.setDeltaMovement(this.getDeltaMovement().multiply(0.6, 0, 0.6));
		}

		if(level().isClientSide()) return;

		Player nearest = level().getNearestPlayer(this, 4.0);
		if(nearest != null && nearest.isAlive())
		{
			Vec3 diff = nearest.position().add(0, nearest.getBbHeight() * 0.5, 0).subtract(this.position());
			if(diff.lengthSqr() < 1.0)
			{
				nearest.giveExperiencePoints(xpValue);
				this.discard();
				return;
			}
			this.setDeltaMovement(this.getDeltaMovement().scale(0.9).add(diff.normalize().scale(0.1)));
		}
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input)
	{
		xpValue = input.getIntOr("XpValue", 1);
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output)
	{
		output.putInt("XpValue", xpValue);
	}

	@Override
	public boolean hurtServer(net.minecraft.server.level.ServerLevel level, net.minecraft.world.damagesource.DamageSource source, float amount)
	{
		return false;
	}
}
