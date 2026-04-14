package com.ultra.megamod.reliquary.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import com.ultra.megamod.reliquary.entity.TippedArrow;
import com.ultra.megamod.reliquary.item.util.IPotionItem;
import com.ultra.megamod.reliquary.reference.Config;
import com.ultra.megamod.reliquary.util.TooltipBuilder;
import com.ultra.megamod.reliquary.util.potions.PotionEssence;
import com.ultra.megamod.reliquary.util.potions.PotionHelper;
import com.ultra.megamod.reliquary.util.potions.PotionMap;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class TippedArrowItem extends ArrowItem implements IPotionItem, ICreativeTabItemGenerator {
	public TippedArrowItem(Properties properties) {
		super(properties);
	}

	@Override
	public AbstractArrow createArrow(Level level, ItemStack arrowStack, LivingEntity shooter, @Nullable ItemStack projectileWeaponStack) {
		TippedArrow arrowEntity = new TippedArrow(level, shooter, arrowStack, projectileWeaponStack);
		arrowEntity.setPotionEffect(arrowStack);
		return arrowEntity;
	}

	@Override
	public void addCreativeTabItems(Consumer<ItemStack> itemConsumer) {
		if (Boolean.TRUE.equals(Config.COMMON.disable.disablePotions.get())) {
			return;
		}

		for (PotionEssence essence : PotionMap.uniquePotionEssences) {
			ItemStack tippedArrow = new ItemStack(this);
			PotionHelper.addPotionContentsToStack(tippedArrow, PotionHelper.changePotionEffectsDuration(essence.getPotionContents(), 0.125F));

			itemConsumer.accept(tippedArrow);
		}
	}

	@Override
	public void appendHoverText(ItemStack arrow, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
		TooltipBuilder.of(tooltip, context).potionEffects(arrow);
	}

	@Override
	public PotionContents getPotionContents(ItemStack stack) {
		return stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
	}
}
