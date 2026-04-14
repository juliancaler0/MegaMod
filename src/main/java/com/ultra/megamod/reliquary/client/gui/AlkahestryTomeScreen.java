package com.ultra.megamod.reliquary.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.RegistryAccess;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import com.ultra.megamod.reliquary.Reliquary;
import com.ultra.megamod.reliquary.common.gui.AlkahestTomeMenu;
import com.ultra.megamod.reliquary.crafting.AlkahestryRecipeRegistry;
import com.ultra.megamod.reliquary.init.ModItems;

import java.util.List;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class AlkahestryTomeScreen extends BaseScreen<AlkahestTomeMenu> {
	private static final Identifier BOOK_TEX = Reliquary.getRL("textures/gui/book.png");

	public AlkahestryTomeScreen(AlkahestTomeMenu container, Inventory playerInventory, Component title) {
		super(container, playerInventory, title);
	}

	@Override
	protected void renderLabels(GuiGraphics guiGraphics, int x, int y) {
		drawTitleText(guiGraphics);
		drawTomeText(guiGraphics, font);
	}

	@Override
	protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int x, int y) {
		// 1.21.11 blit signature: (RenderPipeline, Identifier, dstX, dstY, uOffset, vOffset, uWidth, vHeight, texWidth, texHeight).
		// The book texture atlas is 146 wide / 190 tall to accommodate the two 10x10 corner ornaments at v=180.
		com.mojang.blaze3d.pipeline.RenderPipeline pipe = net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED;
		guiGraphics.blit(pipe, BOOK_TEX, (width - 146) / 2, (height - 179) / 2, 0, 0, 146, 179, 146, 190);
		guiGraphics.blit(pipe, BOOK_TEX, ((width - 16) / 2) + 19, ((height - 179) / 2) + 148, 0, 180, 10, 10, 146, 190);
		guiGraphics.blit(pipe, BOOK_TEX, ((width - 16) / 2) - 14, ((height - 179) / 2) + 148, 10, 180, 10, 10, 146, 190);

		drawItemStack(guiGraphics, new ItemStack(ModItems.ALKAHESTRY_TOME.get()), (width - 16) / 2, ((height - 179) / 2) + 145);
		ClientLevel level = Minecraft.getInstance().level;
		if (level == null) {
			return;
		}
		RegistryAccess registryAccess = level.registryAccess();
		AlkahestryRecipeRegistry.getDrainRecipe().ifPresent(drainRecipe -> {
			drawItemStack(guiGraphics, drainRecipe.getResultItem(registryAccess), ((width - 16) / 2) - 32, ((height - 179) / 2) + 145);
			drawItemStack(guiGraphics, drainRecipe.getResultItem(registryAccess), ((width - 16) / 2) + 32, ((height - 179) / 2) + 145);
		});
	}

	private void drawTomeText(GuiGraphics guigraphics, Font font) {
		String values = Language.getInstance().getOrDefault("gui.reliquary.alkahestry_tome.text");
		int y = 36 + font.lineHeight;
		for (String value : values.split("\n")) {
			List<FormattedCharSequence> splitText = font.split(Component.literal(value), 100);
			for (FormattedCharSequence text : splitText) {
				int x = (146 - font.width(text)) / 2;
				guigraphics.drawString(font, text, x + 15, y, 0, false);
				y += font.lineHeight;
			}
		}
	}

	private void drawTitleText(GuiGraphics guiGraphics) {
		String values = "Perform basic,\nintermediate or\nadvanced Alkahestry.";
		int count = 1;
		for (String value : values.split("\n")) {
			int x = (146 - font.width(value)) / 2;
			int y = 4 + (count * font.lineHeight);
			guiGraphics.drawString(font, value, x + 15, y, 0, false);
			count++;
		}
	}
}
