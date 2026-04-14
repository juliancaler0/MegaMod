package com.ultra.megamod.reliquary.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.TooltipDisplay;
import com.ultra.megamod.reliquary.item.util.IPotionItem;
import com.ultra.megamod.reliquary.reference.Config;
import com.ultra.megamod.reliquary.util.TooltipBuilder;
import com.ultra.megamod.reliquary.util.potions.PotionEssence;
import com.ultra.megamod.reliquary.util.potions.PotionHelper;
import com.ultra.megamod.reliquary.util.potions.PotionMap;

import java.util.function.Consumer;

public class PotionEssenceItem extends ItemBase implements IPotionItem {

	public PotionEssenceItem(Properties properties) {
		super(properties);
	}

	@Override
	public void addCreativeTabItems(Consumer<ItemStack> itemConsumer) {
		if (Boolean.TRUE.equals(Config.COMMON.disable.disablePotions.get())) {
			return;
		}

		for (PotionEssence essence : PotionMap.uniquePotionEssences) {
			ItemStack essenceItem = new ItemStack(this, 1);
			PotionHelper.addPotionContentsToStack(essenceItem, essence.getPotionContents());

			itemConsumer.accept(essenceItem);
		}
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
		TooltipBuilder.of(tooltip, context).potionEffects(stack);
	}

	@Override
	public PotionContents getPotionContents(ItemStack stack) {
		return stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
	}
}
