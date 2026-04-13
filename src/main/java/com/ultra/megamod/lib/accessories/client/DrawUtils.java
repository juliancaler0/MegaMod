package com.ultra.megamod.lib.accessories.client;

import com.ultra.megamod.lib.accessories.owo.ui.core.Color;
import com.ultra.megamod.lib.accessories.owo.ui.core.OwoUIDrawContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import org.joml.Matrix3x2f;
import org.joml.Vector4f;

public class DrawUtils {

    public static void drawWithSpectrum(GuiGraphics ctx, int x, int y, int blitOffset, int width, int height, Identifier texture, float alpha) {
        TextureAtlasSprite sprite = Minecraft.getInstance().getAtlasManager()
            .getAtlasOrThrow(AtlasIds.GUI)
            .getSprite(texture);

        innerDrawWithSpectrum(ctx, sprite.atlasLocation(), x, x + width, y, y + height, blitOffset, sprite.getU0(), sprite.getU1(), sprite.getV0(), sprite.getV1(), new Vector4f(alpha));
    }

    public static void drawWithSpectrum(GuiGraphics ctx, int x, int y, int blitOffset, int width, int height, TextureAtlasSprite sprite, float alpha) {
        innerDrawWithSpectrum(ctx, sprite.atlasLocation(), x, x + width, y, y + height, blitOffset, sprite.getU0(), sprite.getU1(), sprite.getV0(), sprite.getV1(), new Vector4f(alpha));
    }

    public static void drawWithSpectrum(GuiGraphics ctx, int x, int y, int blitOffset, int width, int height, TextureAtlasSprite sprite, Vector4f alphaValues) {
        innerDrawWithSpectrum(ctx, sprite.atlasLocation(), x, x + width, y, y + height, blitOffset, sprite.getU0(), sprite.getU1(), sprite.getV0(), sprite.getV1(), alphaValues);
    }

    // X: Top Left
    // Y: Top Right
    // Z: Bottom Left
    // W: Bottom Right
    private static void innerDrawWithSpectrum(GuiGraphics guiGraphics, Identifier atlasLocation, int x1, int x2, int y1, int y2, int blitOffset, float minU, float maxU, float minV, float maxV, Vector4f alphaValues) {
        // Spectrum rendering requires private field access - fall back to standard blit with tint
        int alpha = (int) (alphaValues.x * 255);
        int color = (alpha << 24) | 0xFFFFFF;
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, atlasLocation, x1, y1, 0, 0, Math.abs(x2 - x1), Math.abs(y2 - y1), Math.abs(x2 - x1), Math.abs(y2 - y1), Math.abs(x2 - x1), Math.abs(y2 - y1), color);
    }

    public static void drawRectOutlineWithSpectrum(GuiGraphics ctx, int x, int y, int width, int height, float alpha, boolean vertical) {
        innerFill(ctx, x, y, width, 1, alpha, !vertical);
        innerFill(ctx, x, y + height - 1, width, 1, alpha, !vertical);

        innerFill(ctx, x, y + 1, 1, height - 2, alpha, vertical);
        innerFill(ctx, x + width - 1, y + 1, 1, height - 2, alpha, vertical);
    }

    private static void innerFill(GuiGraphics guiGraphics, int x, int y, int width, int height, float alpha, boolean vertical) {
        // TODO: OWO UI gradient rendering not yet ported - requires GradientQuadElementRenderState and OwoUIPipelines
        guiGraphics.fill(x, y, x + width, y + height, new Color(1f, 1f, 1f, alpha).argb());
    }

    private static ScreenRectangle getBounds(int x1, int y1, int x2, int y2, Matrix3x2f matrix3x2f) {
        return new ScreenRectangle(x1, y1, Math.abs(x2 - x1), Math.abs(y2 - y1))/*.transformMaxBounds(matrix3x2f)*/;
    }

    public static void blitSprite(GuiGraphics context, Identifier atlasLocation, int x, int y, int width, int height) {
        blitSprite(context, atlasLocation, x, y, width, height, -1);
    }

    public static void blitSprite(GuiGraphics context, Identifier atlasLocation, int x, int y, int width, int height, int blitOffset) {
        context.blitSprite(RenderPipelines.GUI_TEXTURED, atlasLocation, x, y, width, height, blitOffset);
    }

    public static void blit(GuiGraphics context, Identifier atlasLocation, int x, int y, int width, int height) {
        blit(context, atlasLocation, x, y, 0, 0, width, height, width, height);
    }

    public static void blit(GuiGraphics context, Identifier atlasLocation, int x, int y, int uWidth, int vHeight, int textureWidth, int textureHeight) {
        blit(context, atlasLocation, x, y, 0, 0, uWidth, vHeight, textureWidth, textureHeight);
    }

    public static void blit(GuiGraphics context, Identifier atlasLocation, int x, int y, float uOffset, float vOffset, int uWidth, int vHeight, int textureWidth, int textureHeight) {
        context.blit(RenderPipelines.GUI_TEXTURED, atlasLocation, x, y, uOffset, vOffset, uWidth, vHeight, uWidth, vHeight, textureWidth, textureHeight);
    }

    public static void blit(GuiGraphics context, Identifier atlasLocation, int x, int y, float uOffset, float vOffset, int uWidth, int vHeight, int width, int height, int textureWidth, int textureHeight) {
        context.blit(RenderPipelines.GUI_TEXTURED, atlasLocation, x, y, uOffset, vOffset, uWidth, vHeight, width, height, textureWidth, textureHeight, -1);
    }

    // OwoUIDrawContext overloads
    public static void blit(OwoUIDrawContext context, Identifier atlasLocation, int x, int y, int width, int height) {
        if (context.getGraphics() != null) blit(context.getGraphics(), atlasLocation, x, y, width, height);
    }

    public static void blit(OwoUIDrawContext context, Identifier atlasLocation, int x, int y, float uOffset, float vOffset, int uWidth, int vHeight, int textureWidth, int textureHeight) {
        if (context.getGraphics() != null) blit(context.getGraphics(), atlasLocation, x, y, uOffset, vOffset, uWidth, vHeight, textureWidth, textureHeight);
    }

    public static void blit(OwoUIDrawContext context, Identifier atlasLocation, int x, int y, float uOffset, float vOffset, int uWidth, int vHeight, int width, int height, int textureWidth, int textureHeight) {
        if (context.getGraphics() != null) blit(context.getGraphics(), atlasLocation, x, y, uOffset, vOffset, uWidth, vHeight, width, height, textureWidth, textureHeight);
    }

    public static void blitSprite(OwoUIDrawContext context, Identifier atlasLocation, int x, int y, int width, int height) {
        if (context.getGraphics() != null) blitSprite(context.getGraphics(), atlasLocation, x, y, width, height);
    }

    public static void blitSprite(OwoUIDrawContext context, Identifier atlasLocation, int x, int y, int width, int height, int blitOffset) {
        if (context.getGraphics() != null) blitSprite(context.getGraphics(), atlasLocation, x, y, width, height, blitOffset);
    }

    public static void drawRectOutlineWithSpectrum(OwoUIDrawContext ctx, int x, int y, int width, int height, float alpha, boolean vertical) {
        if (ctx.getGraphics() != null) drawRectOutlineWithSpectrum(ctx.getGraphics(), x, y, width, height, alpha, vertical);
    }

    public static void drawWithSpectrum(OwoUIDrawContext ctx, int x, int y, int blitOffset, int width, int height, Identifier texture, float alpha) {
        if (ctx.getGraphics() != null) drawWithSpectrum(ctx.getGraphics(), x, y, blitOffset, width, height, texture, alpha);
    }

    public static void drawWithSpectrum(OwoUIDrawContext ctx, int x, int y, int blitOffset, int width, int height, TextureAtlasSprite sprite, float alpha) {
        if (ctx.getGraphics() != null) drawWithSpectrum(ctx.getGraphics(), x, y, blitOffset, width, height, sprite, alpha);
    }

    // TODO: THIS CURRENTLY DOSE NOT MAKE THE ICONS WHITE AT ALL AND REQUIRES HEAVY MODIFICATION SIMILAR TO OWO BLUR TO SETUP THE UNIFORMS CORRECTLY
    public static void blitSpriteWithColor(GuiGraphics context, TextureAtlasSprite sprite, int x, int y, int width, int height, int color) {
        context.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, width, height, color);
    }

    public static void blitSpriteWithColor(OwoUIDrawContext context, TextureAtlasSprite sprite, int x, int y, int width, int height, int color) {
        if (context.getGraphics() != null) blitSpriteWithColor(context.getGraphics(), sprite, x, y, width, height, color);
    }

    public static void blitSpriteWithColor(OwoUIDrawContext context, TextureAtlasSprite sprite, int x, int y, int width, int height, com.ultra.megamod.lib.accessories.owo.ui.core.Color color) {
        if (context.getGraphics() != null) blitSpriteWithColor(context.getGraphics(), sprite, x, y, width, height, color.argb());
    }
}
