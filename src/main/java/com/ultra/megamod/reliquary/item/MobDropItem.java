package com.ultra.megamod.reliquary.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import com.ultra.megamod.reliquary.reference.Config;

import java.util.function.Consumer;

public class MobDropItem extends ItemBase {
	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
		if (Boolean.TRUE.equals(Config.COMMON.mobDropsEnabled.get())) {
			super.appendHoverText(stack, context, display, tooltip, flag);
		}
	}
}
