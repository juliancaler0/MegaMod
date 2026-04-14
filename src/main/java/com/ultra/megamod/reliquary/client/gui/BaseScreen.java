package com.ultra.megamod.reliquary.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

/**
 * Helper base class for Reliquary container screens. Handles drawing item
 * stacks at arbitrary screen coordinates and paints the dim-world background
 * before super.render() draws the slots.
 */
public abstract class BaseScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {

	protected BaseScreen(T container, Inventory playerInventory, Component title) {
		super(container, playerInventory, title);
	}

	/**
	 * Draws an ItemStack into the inventory at the given screen coordinates.
	 *
	 * @param stack The ItemStack to be drawn.
	 * @param x     Where the stack will be placed on the x axis.
	 * @param y     Where the stack will be placed on the y axis.
	 */
	protected void drawItemStack(GuiGraphics guiGraphics, ItemStack stack, int x, int y) {
		guiGraphics.renderItem(stack, x, y);
		guiGraphics.renderItemDecorations(font, stack, x, y, null);
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		renderBackground(guiGraphics, mouseX, mouseY, partialTicks);
		super.render(guiGraphics, mouseX, mouseY, partialTicks);
	}
}
