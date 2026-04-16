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

	private record DrawCall(
			Identifier texture,
			int x, int y, int width, int height,
			float minU, float minV, float maxU, float maxV,
			Vector4fc color,
			boolean isFullTexture
	) { }

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
				true
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
		drawCalls.add(new DrawCall(texture, x, y, width, height, minU, minV, maxU, maxV, color, false));
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

			if (call.isFullTexture()) {
				context.blit(RenderPipelines.GUI_TEXTURED, call.texture(),
						call.x(), call.y(),
						0f, 0f,
						call.width(), call.height(),
						call.width(), call.height(),
						call.width(), call.height(),
						argbColor);
			} else {
				// For UV-mapped textures, use blit with explicit UV coordinates
				// blit(pipeline, texture, x, y, uOffset, vOffset, width, height, textureWidth, textureHeight)
				// We need to convert UV (0-1) to pixel coordinates in the texture
				// Use the general blit that accepts float UV offset
				context.blit(RenderPipelines.GUI_TEXTURED, call.texture(),
						call.x(), call.y(),
						call.minU(), call.minV(),
						call.width(), call.height(),
						(int) ((call.maxU() - call.minU()) * call.width() / (call.maxU() - call.minU())),
						(int) ((call.maxV() - call.minV()) * call.height() / (call.maxV() - call.minV())),
						call.width(), call.height(),
						argbColor);
			}
		}
		drawCalls.clear();
	}
}
