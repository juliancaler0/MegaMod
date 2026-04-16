package com.ultra.megamod.lib.pufferfish_skills.client.rendering;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.metadata.gui.GuiSpriteScaling;
import net.minecraft.resources.Identifier;
import org.joml.Vector4fc;

/**
 * Stub for TextureBatchedRenderer - needs full rewrite for 1.21.11 rendering API.
 * TODO: Reimplement texture batch rendering with 1.21.11 vertex consumer API
 */
public class TextureBatchedRenderer {

	public void emitTexture(
			GuiGraphics context, Identifier texture,
			int x, int y, int width, int height,
			Vector4fc color
	) {
		// TODO: 1.21.11 rendering migration
	}

	public void emitSprite(
			GuiGraphics context, TextureAtlasSprite sprite, GuiSpriteScaling scaling,
			int x, int y, int width, int height,
			Vector4fc color
	) {
		// TODO: 1.21.11 rendering migration
	}

	public void draw(GuiGraphics context) {
		// TODO: 1.21.11 rendering migration
	}
}
