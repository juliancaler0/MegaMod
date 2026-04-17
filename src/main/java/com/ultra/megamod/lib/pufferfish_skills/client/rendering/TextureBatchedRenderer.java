package com.ultra.megamod.lib.pufferfish_skills.client.rendering;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.metadata.gui.GuiSpriteScaling;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import org.joml.Vector4fc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TextureBatchedRenderer {
	private final List<DrawCall> drawCalls = new ArrayList<>();

	private enum DrawMode {
		/** Standalone PNG textured blit — reads the whole PNG at the given identifier and stretches it into (x,y)/(width,height). */
		FULL_TEXTURE,
		/** Sprite-atlas blit — the identifier is a sprite path on the GUI sprite atlas (e.g. {@code advancements/task_frame_obtained}, {@code mob_effect/speed}). */
		SPRITE,
		/** UV-subregion blit on a standalone PNG. */
		SUBREGION
	}

	private record DrawCall(
			Identifier texture,
			int x, int y, int width, int height,
			float minU, float minV, float maxU, float maxV,
			Vector4fc color,
			DrawMode mode
	) { }

	/**
	 * Emits a standalone-PNG full-texture blit. The {@code texture} identifier resolves to
	 * {@code assets/<ns>/<path>} and is loaded synchronously by {@link net.minecraft.client.renderer.texture.TextureManager#getTexture}.
	 * For sprite-atlas paths (like {@code mob_effect/speed} or {@code advancements/task_frame_obtained}) use
	 * {@link #emitSpriteByIdentifier} instead.
	 */
	public void emitTexture(
			GuiGraphics context, Identifier texture,
			int x, int y, int width, int height,
			Vector4fc color
	) {
		drawCalls.add(new DrawCall(
				texture,
				x, y, width, height,
				0f, 0f, 1f, 1f,
				color,
				DrawMode.FULL_TEXTURE
		));
	}

	/**
	 * Emits a GUI-sprite-atlas blit. Use for identifiers that are sprite paths on the GUI sprite
	 * atlas, i.e. identifiers without {@code textures/gui/} prefix or {@code .png} suffix, such as
	 * {@code minecraft:advancements/task_frame_obtained} or {@code minecraft:mob_effect/speed}.
	 */
	public void emitSpriteByIdentifier(
			GuiGraphics context, Identifier sprite,
			int x, int y, int width, int height,
			Vector4fc color
	) {
		drawCalls.add(new DrawCall(
				sprite,
				x, y, width, height,
				0f, 0f, 1f, 1f,
				color,
				DrawMode.SPRITE
		));
	}

	public void emitSprite(
			GuiGraphics context, TextureAtlasSprite sprite, GuiSpriteScaling scaling,
			int x, int y, int width, int height,
			Vector4fc color
	) {
		if (scaling instanceof GuiSpriteScaling.Stretch) {
			emitSpriteStretch(sprite, x, y, width, height, color);
		} else if (scaling instanceof GuiSpriteScaling.Tile tile) {
			emitSpriteTile(sprite, tile, x, y, width, height, color);
		} else if (scaling instanceof GuiSpriteScaling.NineSlice nineSlice) {
			emitSpriteNineSlice(sprite, nineSlice, x, y, width, height, color);
		}
	}

	private void emitSpriteTile(
			TextureAtlasSprite sprite, GuiSpriteScaling.Tile tile,
			int x, int y, int width, int height,
			Vector4fc color
	) {
		if (width <= 0 || height <= 0 || tile.width() <= 0 || tile.height() <= 0) {
			return;
		}
		for (var tileX = 0; tileX < width; tileX += tile.width()) {
			var tileWidth = Math.min(tile.width(), width - tileX);
			for (var tileY = 0; tileY < height; tileY += tile.height()) {
				var tileHeight = Math.min(tile.height(), height - tileY);
				emitSpriteStretch(sprite, x + tileX, y + tileY, tileWidth, tileHeight, color);
			}
		}
	}

	private void emitSpriteNineSlice(
			TextureAtlasSprite sprite, GuiSpriteScaling.NineSlice nineSlice,
			int x, int y, int width, int height,
			Vector4fc color
	) {
		if (width == nineSlice.width() && height == nineSlice.height()) {
			emitSpriteStretch(sprite, x, y, width, height, color);
			return;
		}

		var border = nineSlice.border();
		var left = Math.min(border.left(), width / 2);
		var top = Math.min(border.top(), height / 2);
		var right = Math.min(border.right(), width / 2);
		var bottom = Math.min(border.bottom(), height / 2);

		if (width == nineSlice.width()) {
			emitTextureBatched(sprite.atlasLocation(), x, y, width, top,
					sprite.getU0(), sprite.getV0(), sprite.getU1(),
					getFrameV(sprite, (float) top / nineSlice.height()), color);

			for (var tileY = top; tileY < height - bottom; tileY += nineSlice.height() - top - bottom) {
				var tileHeight = Math.min(nineSlice.height() - top - bottom, height - bottom - tileY);
				emitTextureBatched(sprite.atlasLocation(), x, y + tileY, nineSlice.width(), tileHeight,
						sprite.getU0(), getFrameV(sprite, (float) top / nineSlice.height()),
						sprite.getU1(), getFrameV(sprite, (float) (top + tileHeight) / nineSlice.height()), color);
			}

			emitTextureBatched(sprite.atlasLocation(), x, y + height - bottom, width, bottom,
					sprite.getU0(), getFrameV(sprite, (float) (nineSlice.height() - bottom) / nineSlice.height()),
					sprite.getU1(), sprite.getV1(), color);
			return;
		}

		if (height == nineSlice.height()) {
			emitTextureBatched(sprite.atlasLocation(), x, y, left, height,
					sprite.getU0(), sprite.getV0(), getFrameU(sprite, (float) left / nineSlice.width()),
					sprite.getV1(), color);

			for (var tileX = left; tileX < width - right; tileX += nineSlice.width() - left - right) {
				var tileWidth = Math.min(nineSlice.width() - left - right, width - right - tileX);
				emitTextureBatched(sprite.atlasLocation(), x + tileX, y, tileWidth, nineSlice.height(),
						getFrameU(sprite, (float) left / nineSlice.width()), sprite.getV0(),
						getFrameU(sprite, (float) (left + tileWidth) / nineSlice.width()), sprite.getV1(), color);
			}

			emitTextureBatched(sprite.atlasLocation(), x + width - right, y, right, height,
					getFrameU(sprite, (float) (nineSlice.width() - right) / nineSlice.width()), sprite.getV0(),
					sprite.getU1(), sprite.getV1(), color);
			return;
		}

		// top left
		emitTextureBatched(sprite.atlasLocation(), x, y, left, top,
				sprite.getU0(), sprite.getV0(),
				getFrameU(sprite, (float) left / nineSlice.width()),
				getFrameV(sprite, (float) top / nineSlice.height()), color);
		// top right
		emitTextureBatched(sprite.atlasLocation(), x + width - right, y, right, top,
				getFrameU(sprite, (float) (nineSlice.width() - right) / nineSlice.width()), sprite.getV0(),
				sprite.getU1(), getFrameV(sprite, (float) top / nineSlice.height()), color);
		// bottom right
		emitTextureBatched(sprite.atlasLocation(), x + width - right, y + height - bottom, right, bottom,
				getFrameU(sprite, (float) (nineSlice.width() - right) / nineSlice.width()),
				getFrameV(sprite, (float) (nineSlice.height() - bottom) / nineSlice.height()),
				sprite.getU1(), sprite.getV1(), color);
		// bottom left
		emitTextureBatched(sprite.atlasLocation(), x, y + height - bottom, left, bottom,
				sprite.getU0(), getFrameV(sprite, (float) (nineSlice.height() - bottom) / nineSlice.height()),
				getFrameU(sprite, (float) left / nineSlice.width()), sprite.getV1(), color);

		// top and bottom edges
		for (var tileX = left; tileX < width - right; tileX += nineSlice.width() - left - right) {
			var tileWidth = Math.min(nineSlice.width() - left - right, width - right - tileX);
			emitTextureBatched(sprite.atlasLocation(), x + tileX, y, tileWidth, top,
					getFrameU(sprite, (float) left / nineSlice.width()), sprite.getV0(),
					getFrameU(sprite, (float) (left + tileWidth) / nineSlice.width()),
					getFrameV(sprite, (float) top / nineSlice.height()), color);
			emitTextureBatched(sprite.atlasLocation(), x + tileX, y + height - bottom, tileWidth, bottom,
					getFrameU(sprite, (float) left / nineSlice.width()),
					getFrameV(sprite, (float) (nineSlice.height() - bottom) / nineSlice.height()),
					getFrameU(sprite, (float) (left + tileWidth) / nineSlice.width()), sprite.getV1(), color);
		}

		// left and right edges
		for (var tileY = top; tileY < height - bottom; tileY += nineSlice.height() - top - bottom) {
			var tileHeight = Math.min(nineSlice.height() - top - bottom, height - bottom - tileY);
			emitTextureBatched(sprite.atlasLocation(), x, y + tileY, left, tileHeight,
					sprite.getU0(), getFrameV(sprite, (float) top / nineSlice.height()),
					getFrameU(sprite, (float) left / nineSlice.width()),
					getFrameV(sprite, (float) (top + tileHeight) / nineSlice.height()), color);
			emitTextureBatched(sprite.atlasLocation(), x + width - right, y + tileY, right, tileHeight,
					getFrameU(sprite, (float) (nineSlice.width() - right) / nineSlice.width()),
					getFrameV(sprite, (float) top / nineSlice.height()),
					sprite.getU1(), getFrameV(sprite, (float) (top + tileHeight) / nineSlice.height()), color);
		}

		// middle
		for (var tileX = left; tileX < width - right; tileX += nineSlice.width() - left - right) {
			var tileWidth = Math.min(nineSlice.width() - left - right, width - right - tileX);
			for (var tileY = top; tileY < height - bottom; tileY += nineSlice.height() - top - bottom) {
				var tileHeight = Math.min(nineSlice.height() - top - bottom, height - bottom - tileY);
				emitTextureBatched(sprite.atlasLocation(), x + tileX, y + tileY, tileWidth, tileHeight,
						getFrameU(sprite, (float) left / nineSlice.width()),
						getFrameV(sprite, (float) top / nineSlice.height()),
						getFrameU(sprite, (float) (left + tileWidth) / nineSlice.width()),
						getFrameV(sprite, (float) (top + tileHeight) / nineSlice.height()), color);
			}
		}
	}

	private void emitSpriteStretch(
			TextureAtlasSprite sprite,
			int x, int y, int width, int height,
			Vector4fc color
	) {
		emitTextureBatched(
				sprite.atlasLocation(),
				x, y, width, height,
				sprite.getU0(), sprite.getV0(), sprite.getU1(), sprite.getV1(),
				color
		);
	}

	private void emitTextureBatched(
			Identifier texture,
			int x, int y, int width, int height,
			float minU, float minV, float maxU, float maxV,
			Vector4fc color
	) {
		drawCalls.add(new DrawCall(texture, x, y, width, height, minU, minV, maxU, maxV, color, DrawMode.SUBREGION));
	}

	private static float getFrameU(TextureAtlasSprite sprite, float frame) {
		return sprite.getU0() + (sprite.getU1() - sprite.getU0()) * frame;
	}

	private static float getFrameV(TextureAtlasSprite sprite, float frame) {
		return sprite.getV0() + (sprite.getV1() - sprite.getV0()) * frame;
	}

	public void draw(GuiGraphics context) {
		for (var call : drawCalls) {
			int argbColor = ARGB.colorFromFloat(call.color().w(), call.color().x(), call.color().y(), call.color().z());

			switch (call.mode()) {
				case FULL_TEXTURE -> {
					// Standalone PNG: blit the entire texture into the target rect.
					// Passing width==textureWidth makes u0=0, u1=1 so the full image maps to the target.
					context.blit(RenderPipelines.GUI_TEXTURED, call.texture(),
							call.x(), call.y(),
							0f, 0f,
							call.width(), call.height(),
							call.width(), call.height(),
							argbColor);
				}
				case SPRITE -> {
					// GUI-sprite-atlas sprite: use blitSprite so the identifier resolves through
					// the sprite atlas (mob_effect/*, advancements/*, hud/*, etc.).
					context.blitSprite(RenderPipelines.GUI_TEXTURED, call.texture(),
							call.x(), call.y(),
							call.width(), call.height(),
							argbColor);
				}
				case SUBREGION -> {
					// UV-subregion of a standalone PNG. Convert normalized UV (0-1) to pixel offsets
					// based on (call.width, call.height) being treated as the underlying image size.
					int u = (int) (call.minU() * call.width());
					int v = (int) (call.minV() * call.height());
					int uSpan = (int) ((call.maxU() - call.minU()) * call.width());
					int vSpan = (int) ((call.maxV() - call.minV()) * call.height());
					context.blit(RenderPipelines.GUI_TEXTURED, call.texture(),
							call.x(), call.y(),
							(float) u, (float) v,
							call.width(), call.height(),
							uSpan, vSpan,
							call.width(), call.height(),
							argbColor);
				}
			}
		}
		drawCalls.clear();
	}
}
