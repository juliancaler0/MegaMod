package com.ultra.megamod.reliquary.item;

import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.level.Level;
import com.ultra.megamod.reliquary.entity.potion.FertilePotion;
import com.ultra.megamod.reliquary.init.ModItems;
import com.ultra.megamod.reliquary.reference.Config;

public class FertilePotionItem extends ItemBase implements ProjectileItem {
	public FertilePotionItem() {
		super(new Properties(), Config.COMMON.disable.disablePotions);
	}

	// TODO: 1.21.11 port - Item#getCraftingRemainingItem removed; set via Properties#craftRemainder.

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);

		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}
		if (!player.isCreative()) {
			stack.shrink(1);
		}
		level.playSound(null, player.blockPosition(), SoundEvents.SPLASH_POTION_THROW, SoundSource.NEUTRAL, 0.5F, 0.4F / (level.random.nextFloat() * 0.4F + 0.8F));

		FertilePotion fertilePotion = new FertilePotion(level, player);
		fertilePotion.shootFromRotation(player, player.getXRot(), player.getYRot(), -20.0F, 0.7F, 1.0F);
		level.addFreshEntity(fertilePotion);
		return InteractionResult.SUCCESS;
	}

	@Override
	public Projectile asProjectile(Level level, Position position, ItemStack itemStack, Direction direction) {
		return new FertilePotion(level, position);
	}
}
