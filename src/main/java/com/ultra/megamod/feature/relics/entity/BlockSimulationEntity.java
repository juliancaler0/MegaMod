package com.ultra.megamod.feature.relics.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class BlockSimulationEntity extends Entity
{
	private int lifespan = 40;

	public BlockSimulationEntity(EntityType<? extends BlockSimulationEntity> type, Level level)
	{
		super(type, level);
	}

	public BlockSimulationEntity(Level level, double x, double y, double z, int lifespan)
	{
		super(RelicEntityRegistry.BLOCK_SIMULATION.get(), level);
		this.setPos(x, y, z);
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
		}
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input)
	{
		lifespan = input.getIntOr("Lifespan", 40);
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output)
	{
		output.putInt("Lifespan", lifespan);
	}

	@Override
	public boolean hurtServer(net.minecraft.server.level.ServerLevel level, net.minecraft.world.damagesource.DamageSource source, float amount)
	{
		return false;
	}
}
