package com.ultra.megamod.reliquary.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import com.ultra.megamod.reliquary.handler.CommonEventHandler;
import com.ultra.megamod.reliquary.handler.HandlerPriority;
import com.ultra.megamod.reliquary.handler.IPlayerHurtHandler;
import com.ultra.megamod.reliquary.init.ModItems;
import com.ultra.megamod.reliquary.reference.Config;
import com.ultra.megamod.reliquary.util.InventoryHelper;

public class InfernalClawsItem extends ItemBase {
	public InfernalClawsItem(Properties properties) {
		super(properties.stacksTo(1));

		CommonEventHandler.registerPlayerHurtHandler(new IPlayerHurtHandler() {
			@Override
			public boolean canApply(Player player, LivingIncomingDamageEvent event) {
				return (event.getSource() == player.damageSources().inFire() || event.getSource() == player.damageSources().onFire())
						&& player.getFoodData().getFoodLevel() > 0
						&& InventoryHelper.playerHasItem(player, ModItems.INFERNAL_CLAWS.get());

			}

			@Override
			public boolean apply(Player player, LivingIncomingDamageEvent event) {
				player.causeFoodExhaustion(event.getAmount() * ((float) Config.COMMON.items.infernalClaws.hungerCostPercent.get() / 100F));
				return true;
			}

			@Override
			public HandlerPriority getPriority() {
				return HandlerPriority.HIGH;
			}
		});
	}

	@Override
	public net.minecraft.network.chat.Component getName(ItemStack stack) {
		return super.getName(stack).copy().withStyle(ChatFormatting.RED);
	}

	@Override
	public boolean isFoil(ItemStack stack) {
		return true;
	}

	// this item's effects are handled in events
}
