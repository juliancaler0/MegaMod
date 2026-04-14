package com.ultra.megamod.reliquary.util;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import com.ultra.megamod.reliquary.Reliquary;
import com.ultra.megamod.reliquary.item.EnderStaffItem;

import java.util.function.Consumer;
import java.util.function.UnaryOperator;

public class TooltipBuilder {
	private static Item.TooltipContext context;
	private final Consumer<Component> tooltip;

	public static TooltipBuilder of(Consumer<Component> tooltip, Item.TooltipContext context) {
		TooltipBuilder.context = context;
		return new TooltipBuilder(tooltip);
	}

	/** Back-compat for callers that still have a {@code List<Component>}. */
	public static TooltipBuilder of(java.util.List<Component> tooltip, Item.TooltipContext context) {
		TooltipBuilder.context = context;
		return new TooltipBuilder(tooltip::add);
	}

	private TooltipBuilder(Consumer<Component> tooltip) {
		this.tooltip = tooltip;
	}

	public void potionEffects(PotionContents potionContents) {
		// Port note (1.21.11): the old instance-method PotionContents#addPotionTooltip was
		// replaced by the static overload that takes the resolved effect list directly and the
		// Consumer<Component> sink. We drive it from PotionContents#getAllEffects() which
		// preserves the original "display every effect this bottle will apply" behaviour.
		PotionContents.addPotionTooltip(potionContents.getAllEffects(), tooltip, 1f, context.tickRate());
	}

	public void potionEffects(ItemStack stack) {
		potionEffects(stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY));
	}

	public TooltipBuilder itemTooltip(Item item) {
		String langName = item.getDescriptionId() + ".tooltip";
		if (Language.getInstance().has(langName)) {
			addTooltipLines(c -> c.withStyle(ChatFormatting.GRAY), item.getDescriptionId() + ".tooltip");
		}
		return this;
	}

	public TooltipBuilder charge(Item item, String langSuffix, int charge, int chargeLimit) {
		tooltip.accept(Component.translatable(
						item.getDescriptionId() + langSuffix,
						Component.literal(String.valueOf(charge)).withStyle(ChatFormatting.WHITE),
						Component.literal(String.valueOf(chargeLimit)).withStyle(ChatFormatting.BLUE))
				.withStyle(ChatFormatting.GREEN));
		return this;
	}

	public TooltipBuilder data(Item item, String langSuffix, Object... args) {
		return data(item.getDescriptionId() + langSuffix, args);
	}

	public TooltipBuilder data(String langKey, Object... args) {
		Component[] components;
		if (args.length > 0) {
			components = new Component[args.length];
			for (int i = 0, argsLength = args.length; i < argsLength; i++) {
				Object arg = args[i];
				if (arg instanceof Component argComponent) {
					components[i] = argComponent;
				} else {
					components[i] = Component.literal(String.valueOf(arg)).withStyle(ChatFormatting.WHITE);
				}
			}
		} else {
			components = new Component[0];
		}

		tooltip.accept(Component.translatable(langKey, components).withStyle(ChatFormatting.GREEN));
		return this;
	}

	public TooltipBuilder charge(Item item, String langSuffix, String chargeName, int charge) {
		tooltip.accept(Component.translatable(
						item.getDescriptionId() + langSuffix,
						Component.literal(chargeName).withStyle(ChatFormatting.WHITE),
						Component.literal(String.valueOf(charge)).withStyle(ChatFormatting.WHITE))
				.withStyle(ChatFormatting.GREEN));
		return this;
	}

	public TooltipBuilder charge(Item item, String langSuffix, int charge) {
		tooltip.accept(Component.translatable(
						item.getDescriptionId() + langSuffix,
						Component.literal(String.valueOf(charge)).withStyle(ChatFormatting.WHITE))
				.withStyle(ChatFormatting.GREEN));
		return this;
	}

	public TooltipBuilder showMoreInfo() {
		if (!isShiftDown()) {
			tooltip.accept(Component.translatable("tooltip." + Reliquary.MOD_ID + ".hold_for_more_info",
					Component.translatable("tooltip." + Reliquary.MOD_ID + ".shift").withStyle(ChatFormatting.AQUA)
			).withStyle(ChatFormatting.DARK_GRAY));
		}
		return this;
	}

	/**
	 * Port note (1.21.11): net.minecraft.client.gui.screens.Screen#hasShiftDown was removed. We
	 * query InputConstants directly against the active window's GLFW handle, which is the same
	 * check Screen used internally. The try/catch guard handles the rare case (dedicated-server
	 * tooltip rendering in some test harnesses) where no window is attached.
	 */
	private static boolean isShiftDown() {
		try {
			com.mojang.blaze3d.platform.Window window = Minecraft.getInstance().getWindow();
			return InputConstants.isKeyDown(window, InputConstants.KEY_LSHIFT)
					|| InputConstants.isKeyDown(window, InputConstants.KEY_RSHIFT);
		} catch (Throwable ignored) {
			return false;
		}
	}

	public TooltipBuilder absorb() {
		tooltip.accept(Component.translatable("tooltip." + Reliquary.MOD_ID + ".absorb").withStyle(ChatFormatting.DARK_GRAY));
		return this;
	}

	public TooltipBuilder absorbActive(String itemName) {
		return absorbActive(Component.literal(itemName).withStyle(ChatFormatting.DARK_AQUA));
	}

	public TooltipBuilder absorbActive(Component thingName) {
		tooltip.accept(Component.translatable("tooltip." + Reliquary.MOD_ID + ".absorb_active", thingName).withStyle(ChatFormatting.DARK_GRAY));
		return this;
	}

	public TooltipBuilder description(String langKey, Object... args) {
		addTooltipLines(c -> c.withStyle(ChatFormatting.DARK_GRAY), langKey, args);
		return this;
	}

	public TooltipBuilder description(Item item, String langSuffix, Object... args) {
		return description(item.getDescriptionId() + langSuffix, args);
	}

	public TooltipBuilder warning(EnderStaffItem enderStaffItem, String langSuffix) {
		tooltip.accept(Component.translatable(enderStaffItem.getDescriptionId() + langSuffix).withStyle(ChatFormatting.RED));
		return this;
	}

	private void addTooltipLines(UnaryOperator<MutableComponent> applyStyle, String langKey, Object... args) {
		String text = Language.getInstance().getOrDefault(langKey);
		String[] lines = text.split("\n");
		for (String line : lines) {
			tooltip.accept(applyStyle.apply(Component.translatableWithFallback("", line, args)));
		}
	}
}
