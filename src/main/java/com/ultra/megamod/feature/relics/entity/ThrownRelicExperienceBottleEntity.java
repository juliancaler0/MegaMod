package com.ultra.megamod.feature.relics.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.HitResult;

public class ThrownRelicExperienceBottleEntity extends ThrowableItemProjectile
{
	public ThrownRelicExperienceBottleEntity(EntityType<? extends ThrownRelicExperienceBottleEntity> type, Level level)
	{
		super(type, level);
	}

	@Override
	protected Item getDefaultItem()
	{
		return Items.EXPERIENCE_BOTTLE;
	}

	@Override
	protected void onHit(HitResult result)
	{
		super.onHit(result);
		if(!level().isClientSide())
		{
			int amount = 3 + random.nextInt(5);
			for(int i = 0; i < amount; i++)
			{
				RelicExperienceOrbEntity orb = new RelicExperienceOrbEntity(level(), getX(), getY() + 0.2, getZ(), 1);
				orb.setDeltaMovement(
					(random.nextDouble() - 0.5) * 0.3,
					0.25 + random.nextDouble() * 0.2,
					(random.nextDouble() - 0.5) * 0.3
				);
				level().addFreshEntity(orb);
			}
			this.discard();
		}
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output)
	{
		super.addAdditionalSaveData(output);
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input)
	{
		super.readAdditionalSaveData(input);
	}
}
