package com.ultra.megamod.reliquary.client.gui.components;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;
import com.ultra.megamod.reliquary.Reliquary;

/**
 * Thin 74×11 vertical XP bar used by the Hero Medallion HUD. The reference
 * implementation drove this with raw BufferBuilder quads, but GuiGraphics.blit
 * handles the same work via the modern texture-atlas system, so the bar is
 * now rendered through GuiGraphics for simplicity and API stability.
 */
public class XPBarPane extends Component {
	private static final Identifier XP_BAR = Reliquary.getRL("textures/gui/xp_bar.png");
	private float xpRatio;

	public void setXpRatio(float xpRatio) {
		this.xpRatio = xpRatio;
	}

	@Override
	public int getHeightInternal() {
		return 74;
	}

	@Override
	public int getWidthInternal() {
		return 11;
	}

	@Override
	public int getPadding() {
		return 2;
	}

	@Override
	public void renderInternal(GuiGraphics guiGraphics, int x, int y) {
		com.mojang.blaze3d.pipeline.RenderPipeline pipe = net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED;
		// Background (unfilled) bar
		guiGraphics.blit(pipe, XP_BAR, x, y, 0, 0, 11, 74, 22, 74);

		if (xpRatio > 0) {
			int filledHeight = (int) (xpRatio * 74);
			guiGraphics.blit(pipe, XP_BAR, x, y + (74 - filledHeight), 11, 74 - filledHeight, 11, filledHeight, 22, 74);
		}
	}
}
