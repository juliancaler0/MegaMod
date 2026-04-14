package com.ultra.megamod.reliquary.item;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import org.jetbrains.annotations.Nullable;
import com.ultra.megamod.reliquary.util.TooltipBuilder;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ItemBase extends Item implements ICreativeTabItemGenerator {
	private final Supplier<Boolean> isDisabled;

	public ItemBase() {
		this(new Properties(), () -> false);
	}

	public ItemBase(Supplier<Boolean> isDisabled) {
		this(new Properties(), isDisabled);
	}

	public ItemBase(Properties properties) {
		this(properties, () -> false);
	}

	public ItemBase(Properties properties, Supplier<Boolean> isDisabled) {
		super(properties);
		this.isDisabled = isDisabled;
	}

	@Override
	public void addCreativeTabItems(Consumer<ItemStack> itemConsumer) {
		if (Boolean.TRUE.equals(isDisabled.get())) {
			return;
		}

		itemConsumer.accept(new ItemStack(this));
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
		TooltipBuilder tooltipBuilder = TooltipBuilder.of(tooltip, context).itemTooltip(this);

		if (hasMoreInformation(stack)) {
			tooltipBuilder.showMoreInfo();
			if (isShiftDown()) {
				addMoreInformation(stack, context.registries(), tooltipBuilder);
			}
		}
	}

	/** TODO: 1.21.11 port - Screen#hasShiftDown was removed; query InputConstants directly. */
	protected static boolean isShiftDown() {
		try {
			com.mojang.blaze3d.platform.Window window = Minecraft.getInstance().getWindow();
			return InputConstants.isKeyDown(window, InputConstants.KEY_LSHIFT)
					|| InputConstants.isKeyDown(window, InputConstants.KEY_RSHIFT);
		} catch (Throwable ignored) {
			return false;
		}
	}

	@SuppressWarnings("squid:S1172") //parameter used in overrides
	protected boolean hasMoreInformation(ItemStack stack) {
		return false;
	}

	protected void addMoreInformation(ItemStack stack, @Nullable HolderLookup.Provider registries, TooltipBuilder tooltipBuilder) {
		//overriden in child classes
	}

	@Override
	public Component getName(ItemStack stack) {
		return Component.translatable(getDescriptionId());
	}
}
