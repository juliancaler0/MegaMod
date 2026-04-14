package com.ultra.megamod.feature.relics.entity;

import com.ultra.megamod.feature.relics.effect.RelicEffectRegistry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.EntityHitResult;

public class SolidSnowballEntity extends ThrowableItemProjectile
{
	public SolidSnowballEntity(EntityType<? extends SolidSnowballEntity> type, Level level)
	{
		super(type, level);
	}

	@Override
	protected Item getDefaultItem()
	{
		return Items.SNOWBALL;
	}

	@Override
	protected void onHitEntity(EntityHitResult result)
	{
		super.onHitEntity(result);
		if(!(level() instanceof ServerLevel server)) return;

		if(result.getEntity() instanceof LivingEntity target)
		{
			float damage = 3.0F;
			if(this.getOwner() instanceof Player player)
			{
				target.hurt(server.damageSources().playerAttack(player), damage);
			}
			else
			{
				target.hurt(server.damageSources().magic(), damage);
			}
			target.addEffect(new MobEffectInstance(RelicEffectRegistry.STUN, 30, 0));
			target.knockback(0.6, this.getX() - target.getX(), this.getZ() - target.getZ());
		}
	}

	@Override
	protected void onHit(net.minecraft.world.phys.HitResult result)
	{
		super.onHit(result);
		if(!level().isClientSide())
		{
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
