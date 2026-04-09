package com.ultra.megamod.feature.citizen.blockui;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.metadata.gui.GuiSpriteScaling;
import net.minecraft.client.resources.metadata.gui.GuiSpriteScaling.NineSlice;
import net.minecraft.client.resources.metadata.gui.GuiSpriteScaling.Tile;
import net.minecraft.client.resources.metadata.gui.GuiSpriteScaling.Type;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Matrix3x2fStack;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

/**
 * Our replacement for GuiComponent.
 * Adapted for MC 1.21.11 rendering API - uses simplified rendering without removed
 * RenderSystem.setShader / BufferUploader.drawWithShader pipeline.
 *
 * All fill/blit/gradient operations delegate to vanilla GuiGraphics but apply the BlockOut
 * PoseStack transforms first so coordinates are rendered in BlockOut window space.
 */
public class UiRenderMacros
{
    public static final double HALF_BIAS = 0.5;

    /**
     * Shared GuiGraphics instance set per-frame by BOScreen for use by fill/line methods.
     * This is needed because PoseStack-only methods can no longer directly render.
     */
    private static GuiGraphics currentGuiGraphics;

    /**
     * The BlockOut PoseStack that contains the window offset + scale transforms.
     * Must be applied to currentGuiGraphics before rendering so coordinates map correctly.
     */
    private static PoseStack blockoutPoseStack;

    /**
     * Called by BOScreen at the start of each render to provide access to GuiGraphics
     * and the BlockOut PoseStack that maps window coordinates to screen coordinates.
     */
    public static void setCurrentGuiGraphics(GuiGraphics gg, PoseStack boPoseStack)
    {
        currentGuiGraphics = gg;
        blockoutPoseStack = boPoseStack;
    }

    /**
     * Backward-compatible setter for when no BlockOut PoseStack is available.
     */
    public static void setCurrentGuiGraphics(GuiGraphics gg)
    {
        setCurrentGuiGraphics(gg, null);
    }

    /**
     * Push the current BlockOut transforms onto the vanilla GuiGraphics pose (Matrix3x2fStack).
     * In MC 1.21.11, GuiGraphics.pose() returns a Matrix3x2fStack, not a PoseStack.
     * We extract the 2D translation and scale from the BlockOut 4x4 matrix and apply them.
     */
    private static void pushBlockoutTransform()
    {
        if (currentGuiGraphics != null && blockoutPoseStack != null)
        {
            final Matrix3x2fStack guiPose = currentGuiGraphics.pose();
            guiPose.pushMatrix();
            // Extract translation and scale from the BlockOut 4x4 matrix
            final Matrix4f m = blockoutPoseStack.last().pose();
            // m30, m31 are translation; m00 is scaleX, m11 is scaleY (for axis-aligned transforms)
            guiPose.translate(m.m30(), m.m31());
            guiPose.scale(m.m00(), m.m11());
        }
    }

    /**
     * Pop the BlockOut transforms from the vanilla GuiGraphics pose.
     */
    private static void popBlockoutTransform()
    {
        if (currentGuiGraphics != null && blockoutPoseStack != null)
        {
            currentGuiGraphics.pose().popMatrix();
        }
    }

    // =========================================================================
    //  LINE RECT (border)
    // =========================================================================

    public static void drawLineRectGradient(final PoseStack ps,
        final int x, final int y, final int w, final int h,
        final int argbColorStart, final int argbColorEnd)
    {
        drawLineRectGradient(ps, x, y, w, h, argbColorStart, argbColorEnd, 1);
    }

    public static void drawLineRectGradient(final PoseStack ps,
        final int x, final int y, final int w, final int h,
        final int argbColorStart, final int argbColorEnd, final int lineWidth)
    {
        // Approximate gradient border with solid start color
        drawLineRect(ps, x, y, w, h, argbColorStart, lineWidth);
    }

    public static void drawLineRectGradient(final PoseStack ps,
        final int x, final int y, final int w, final int h,
        final int redStart, final int redEnd, final int greenStart, final int greenEnd,
        final int blueStart, final int blueEnd, final int alphaStart, final int alphaEnd,
        final int lineWidth)
    {
        if (lineWidth < 1 || (alphaStart == 0 && alphaEnd == 0)) return;
        int color = ((alphaStart & 0xff) << 24) | ((redStart & 0xff) << 16) | ((greenStart & 0xff) << 8) | (blueStart & 0xff);
        drawLineRect(ps, x, y, w, h, color, lineWidth);
    }

    public static void drawLineRect(final PoseStack ps, final int x, final int y, final int w, final int h, final int argbColor)
    {
        drawLineRect(ps, x, y, w, h, argbColor, 1);
    }

    public static void drawLineRect(final PoseStack ps,
        final int x, final int y, final int w, final int h,
        final int argbColor, final int lineWidth)
    {
        drawLineRect(ps, x, y, w, h,
            (argbColor >> 16) & 0xff, (argbColor >> 8) & 0xff, argbColor & 0xff,
            (argbColor >> 24) & 0xff, lineWidth);
    }

    public static void drawLineRect(final PoseStack ps,
        final int x, final int y, final int w, final int h,
        final int red, final int green, final int blue, final int alpha,
        final int lineWidth)
    {
        if (lineWidth < 1 || alpha == 0) return;
        int color = ((alpha & 0xff) << 24) | ((red & 0xff) << 16) | ((green & 0xff) << 8) | (blue & 0xff);
        // Draw border as 4 filled rects
        fill(ps, x, y, w, lineWidth, color);                     // top
        fill(ps, x, y + h - lineWidth, w, lineWidth, color);     // bottom
        fill(ps, x, y + lineWidth, lineWidth, h - 2 * lineWidth, color); // left
        fill(ps, x + w - lineWidth, y + lineWidth, lineWidth, h - 2 * lineWidth, color); // right
    }

    // =========================================================================
    //  FILL (solid rect)
    // =========================================================================

    public static void fill(final PoseStack ps, final int x, final int y, final int w, final int h, final int argbColor)
    {
        fill(ps, x, y, w, h, (argbColor >> 16) & 0xff, (argbColor >> 8) & 0xff, argbColor & 0xff, (argbColor >> 24) & 0xff);
    }

    public static void fill(final PoseStack ps,
        final int x, final int y, final int w, final int h,
        final int red, final int green, final int blue, final int alpha)
    {
        if (alpha == 0) return;
        if (currentGuiGraphics != null)
        {
            int argb = ((alpha & 0xff) << 24) | ((red & 0xff) << 16) | ((green & 0xff) << 8) | (blue & 0xff);
            pushBlockoutTransform();
            currentGuiGraphics.fill(x, y, x + w, y + h, argb);
            popBlockoutTransform();
        }
    }

    // =========================================================================
    //  FILL GRADIENT
    // =========================================================================

    public static void fillGradient(final PoseStack ps,
        final int x, final int y, final int w, final int h,
        final int argbColorStart, final int argbColorEnd)
    {
        if (currentGuiGraphics != null)
        {
            pushBlockoutTransform();
            currentGuiGraphics.fillGradient(x, y, x + w, y + h, argbColorStart, argbColorEnd);
            popBlockoutTransform();
        }
    }

    public static void fillGradient(final PoseStack ps,
        final int x, final int y, final int w, final int h,
        final int redStart, final int redEnd, final int greenStart, final int greenEnd,
        final int blueStart, final int blueEnd, final int alphaStart, final int alphaEnd)
    {
        if (alphaStart == 0 && alphaEnd == 0) return;
        int colorStart = ((alphaStart & 0xff) << 24) | ((redStart & 0xff) << 16) | ((greenStart & 0xff) << 8) | (blueStart & 0xff);
        int colorEnd = ((alphaEnd & 0xff) << 24) | ((redEnd & 0xff) << 16) | ((greenEnd & 0xff) << 8) | (blueEnd & 0xff);
        fillGradient(ps, x, y, w, h, colorStart, colorEnd);
    }

    // =========================================================================
    //  LINES
    // =========================================================================

    public static void hLine(final PoseStack ps, final int x, final int xEnd, final int y, final int argbColor)
    {
        fill(ps, Math.min(x, xEnd), y, Math.abs(xEnd - x) + 1, 1, argbColor);
    }

    public static void hLine(final PoseStack ps, final int x, final int xEnd, final int y,
        final int red, final int green, final int blue, final int alpha)
    {
        int color = ((alpha & 0xff) << 24) | ((red & 0xff) << 16) | ((green & 0xff) << 8) | (blue & 0xff);
        hLine(ps, x, xEnd, y, color);
    }

    public static void vLine(final PoseStack ps, final int x, final int y, final int yEnd, final int argbColor)
    {
        fill(ps, x, Math.min(y, yEnd), 1, Math.abs(yEnd - y) + 1, argbColor);
    }

    public static void vLine(final PoseStack ps, final int x, final int y, final int yEnd,
        final int red, final int green, final int blue, final int alpha)
    {
        int color = ((alpha & 0xff) << 24) | ((red & 0xff) << 16) | ((green & 0xff) << 8) | (blue & 0xff);
        vLine(ps, x, y, yEnd, color);
    }

    public static void line(final PoseStack ps, final int x, final int y, final int xEnd, final int yEnd, final int argbColor)
    {
        line(ps, x, y, xEnd, yEnd, (argbColor >> 16) & 0xff, (argbColor >> 8) & 0xff, argbColor & 0xff, (argbColor >> 24) & 0xff);
    }

    public static void line(final PoseStack ps, final int x, final int y, final int xEnd, final int yEnd,
        final int red, final int green, final int blue, final int alpha)
    {
        if (alpha == 0) return;
        // Approximate line as h/v line since diagonal lines require pipeline
        if (y == yEnd)
        {
            int color = ((alpha & 0xff) << 24) | ((red & 0xff) << 16) | ((green & 0xff) << 8) | (blue & 0xff);
            hLine(ps, x, xEnd, y, color);
        }
        else if (x == xEnd)
        {
            int color = ((alpha & 0xff) << 24) | ((red & 0xff) << 16) | ((green & 0xff) << 8) | (blue & 0xff);
            vLine(ps, x, y, yEnd, color);
        }
        // Diagonal lines are no-op for now - requires RenderPipeline port
    }

    // =========================================================================
    //  BLIT (texture rendering)
    // =========================================================================

    public static void blit(final PoseStack ps,
        final Identifier rl, final int x, final int y, final int w, final int h,
        final int u, final int v, final int mapW, final int mapH)
    {
        blit(ps, rl, x, y, w, h, (float) u / mapW, (float) v / mapH, (float) (u + w) / mapW, (float) (v + h) / mapH);
    }

    public static void blit(final PoseStack ps,
        final Identifier rl, final int x, final int y, final int w, final int h,
        final int u, final int v, final int uW, final int vH, final int mapW, final int mapH)
    {
        blit(ps, rl, x, y, w, h, (float) u / mapW, (float) v / mapH, (float) (u + uW) / mapW, (float) (v + vH) / mapH);
    }

    public static void blitSprite(final PoseStack ps,
        final TextureAtlasSprite sprite, final GuiSpriteScaling guiScaling,
        final int x, final int y, final int w, final int h)
    {
        final Identifier atlasLocation = sprite.atlasLocation();
        final float u0 = sprite.getU0();
        final float v0 = sprite.getV0();
        final float u1 = sprite.getU1();
        final float v1 = sprite.getV1();
        if (guiScaling.type() == Type.STRETCH)
        {
            blit(ps, atlasLocation, x, y, w, h, u0, v0, u1, v1);
        }
        else if (guiScaling instanceof final NineSlice nineSlice)
        {
            final int rbW = nineSlice.width();
            final int rbH = nineSlice.height();

            if (rbW == w && rbH == h)
            {
                blit(ps, atlasLocation, x, y, w, h, u0, v0, u1, v1);
            }
            else
            {
                final int uR = nineSlice.border().left();
                final int vR = nineSlice.border().top();
                final int rW = rbW - uR - nineSlice.border().right();
                final int rH = rbH - vR - nineSlice.border().bottom();
                blitRepeatable(ps, atlasLocation, x, y, w, h, u0, v0, u1, v1, uR, vR, rW, rH, rbW, rbH);
            }
        }
        else if (guiScaling instanceof final Tile tile)
        {
            final int tW = tile.width();
            final int tH = tile.height();

            if (tW == w && tH == h)
            {
                blit(ps, atlasLocation, x, y, w, h, u0, v0, u1, v1);
            }
            else
            {
                blitRepeatable(ps, atlasLocation, x, y, w, h, u0, v0, u1, v1, 0, 0, tW, tH, tW, tH);
            }
        }
    }

    public static void blitSprite(final PoseStack ps,
        final TextureAtlasSprite sprite,
        final int x, final int y, final int w, final int h)
    {
        blit(ps, sprite.atlasLocation(), x, y, w, h, sprite.getU0(), sprite.getV0(), sprite.getU1(), sprite.getV1());
    }

    public static void blit(final PoseStack ps, final Identifier rl, final int x, final int y, final int w, final int h)
    {
        blit(ps, rl, x, y, w, h, 0.0f, 0.0f, 1.0f, 1.0f);
    }

    public static void blit(final PoseStack ps,
        final Identifier rl, final int x, final int y, final int w, final int h,
        final float uMin, final float vMin, final float uMax, final float vMax)
    {
        if (currentGuiGraphics != null)
        {
            pushBlockoutTransform();
            // GuiGraphics.blit in 1.21.11 takes float UV offset and int texture dimensions.
            // Convert normalized UV (0..1) to texel-space by computing virtual texture dimensions.
            final float uRange = uMax - uMin;
            final float vRange = vMax - vMin;
            int texW = uRange > 0.001f ? Math.round(w / uRange) : w;
            int texH = vRange > 0.001f ? Math.round(h / vRange) : h;
            float uStart = uMin * texW;
            float vStart = vMin * texH;
            currentGuiGraphics.blit(RenderPipelines.GUI_TEXTURED, rl, x, y, uStart, vStart, w, h, texW, texH);
            popBlockoutTransform();
        }
    }

    /**
     * Draws texture without scaling using repeatable texture center.
     */
    protected static void blitRepeatable(final PoseStack ps,
        final Identifier rl, final int x, final int y, final int width, final int height,
        final float uMin, final float vMin, final float uMax, final float vMax,
        final int uRepeat, final int vRepeat, final int repeatWidth, final int repeatHeight,
        final int repeatBoxWidth, final int repeatBoxHeight)
    {
        if (uRepeat < 0 || vRepeat < 0 ||
            uRepeat >= repeatBoxWidth ||
            vRepeat >= repeatBoxHeight ||
            repeatWidth < 1 ||
            repeatHeight < 1 ||
            repeatWidth > repeatBoxWidth - uRepeat ||
            repeatHeight > repeatBoxHeight - vRepeat)
        {
            // Invalid repeatable box, fallback to stretch
            blit(ps, rl, x, y, width, height, uMin, vMin, uMax, vMax);
            return;
        }

        final int repeatCountX = Math.max(1, Math.max(0, width - (repeatBoxWidth - repeatWidth)) / repeatWidth);
        final int repeatCountY = Math.max(1, Math.max(0, height - (repeatBoxHeight - repeatHeight)) / repeatHeight);
        final float uTexelWidth = (uMax - uMin) / repeatBoxWidth;
        final float vTexelHeight = (vMax - vMin) / repeatBoxHeight;

        // main
        for (int i = 0; i < repeatCountX; i++)
        {
            final int uAdjust = i == 0 ? 0 : uRepeat;
            final int xStart = x + uAdjust + i * repeatWidth;
            final int w = Math.min(repeatWidth + uRepeat - uAdjust, width - (repeatBoxWidth - uRepeat - repeatWidth));
            final float minU = uMin + uTexelWidth * uAdjust;
            final float maxU = minU + uTexelWidth * w;

            for (int j = 0; j < repeatCountY; j++)
            {
                final int vAdjust = j == 0 ? 0 : vRepeat;
                final int yStart = y + vAdjust + j * repeatHeight;
                final int h = Math.min(repeatHeight + vRepeat - vAdjust, height - (repeatBoxHeight - vRepeat - repeatHeight));
                final float minV = vMin + vTexelHeight * vAdjust;
                final float maxV = minV + vTexelHeight * h;

                blit(ps, rl, xStart, yStart, w, h, minU, minV, maxU, maxV);
            }
        }

        final int xEnd = x + Math.min(uRepeat + repeatCountX * repeatWidth, width - (repeatBoxWidth - uRepeat - repeatWidth));
        final int yEnd = y + Math.min(vRepeat + repeatCountY * repeatHeight, height - (repeatBoxHeight - vRepeat - repeatHeight));
        final int uLeft = width - (xEnd - x);
        final int vBot = height - (yEnd - y);
        final float restMinU = uMax - uLeft * uTexelWidth;
        final float restMinV = vMax - vBot * vTexelHeight;

        // bot border
        for (int i = 0; i < repeatCountX; i++)
        {
            final int uAdjust = i == 0 ? 0 : uRepeat;
            final int xStart = x + uAdjust + i * repeatWidth;
            final int w = Math.min(repeatWidth + uRepeat - uAdjust, width - uLeft);
            final float minU = uMin + uTexelWidth * uAdjust;
            final float maxU = minU + uTexelWidth * w;

            blit(ps, rl, xStart, yEnd, w, vBot, minU, restMinV, maxU, vMax);
        }

        // right border
        for (int j = 0; j < repeatCountY; j++)
        {
            final int vAdjust = j == 0 ? 0 : vRepeat;
            final int yStart = y + vAdjust + j * repeatHeight;
            final int h = Math.min(repeatHeight + vRepeat - vAdjust, height - vBot);
            final float minV = vMin + vTexelHeight * vAdjust;
            final float maxV = minV + vTexelHeight * h;

            blit(ps, rl, xEnd, yStart, uLeft, h, restMinU, minV, uMax, maxV);
        }

        // bottom right corner
        blit(ps, rl, xEnd, yEnd, uLeft, vBot, restMinU, restMinV, uMax, vMax);
    }

    // =========================================================================
    //  TRIANGLE POPULATION (for custom vertex rendering)
    // =========================================================================

    // These methods are kept for API compatibility but are no-ops since the
    // BufferBuilder/BufferUploader pipeline has been removed in 1.21.11.
    // They will need a full port to RenderPipeline when custom vertex rendering is needed.

    public static void populateFillTriangles(final Matrix4f m,
        final Object buffer, final int x, final int y, final int w, final int h,
        final int red, final int green, final int blue, final int alpha)
    {
        // No-op: requires RenderPipeline port
    }

    public static void populateFillGradientTriangles(final Matrix4f m,
        final Object buffer, final int x, final int y, final int w, final int h,
        final int redStart, final int redEnd, final int greenStart, final int greenEnd,
        final int blueStart, final int blueEnd, final int alphaStart, final int alphaEnd)
    {
        // No-op: requires RenderPipeline port
    }

    public static void populateBlitTriangles(final Object buffer,
        final Matrix4f mat, final float xStart, final float xEnd,
        final float yStart, final float yEnd,
        final float uMin, final float uMax, final float vMin, final float vMax)
    {
        // No-op: requires RenderPipeline port
    }

    // =========================================================================
    //  ENTITY RENDERING
    // =========================================================================

    /**
     * Render an entity on a GUI.
     */
    public static void drawEntity(final PoseStack poseStack,
        final int x, final int y, final double scale,
        final float headYaw, final float yaw, final float pitch,
        final Entity entity)
    {
        final LivingEntity livingEntity = (entity instanceof LivingEntity) ? (LivingEntity) entity : null;
        final Minecraft mc = Minecraft.getInstance();
        if (entity.level() == null) return;
        poseStack.pushPose();
        poseStack.translate((float) x, (float) y, 1050.0F);
        poseStack.scale(1.0F, 1.0F, -1.0F);
        poseStack.translate(0.0D, 0.0D, 1000.0D);
        poseStack.scale((float) scale, (float) scale, (float) scale);
        final Quaternionf pitchRotation = Axis.XP.rotationDegrees(pitch);
        poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
        poseStack.mulPose(pitchRotation);
        final float oldYaw = entity.getYRot();
        final float oldPitch = entity.getXRot();
        final float oldYawOffset = livingEntity == null ? 0F : livingEntity.yBodyRot;
        final float oldPrevYawHead = livingEntity == null ? 0F : livingEntity.yHeadRotO;
        final float oldYawHead = livingEntity == null ? 0F : livingEntity.yHeadRot;
        entity.setYRot(180.0F + (float) headYaw);
        entity.setXRot(-pitch);
        if (livingEntity != null)
        {
            livingEntity.yBodyRot = 180.0F + yaw;
            livingEntity.yHeadRot = entity.getYRot();
            livingEntity.yHeadRotO = entity.getYRot();
        }
        // Entity rendering APIs changed significantly in 1.21.11:
        // Lighting.setupForEntityInInventory(), RenderSystem.runAsFancy(),
        // dispatcher.setRenderShadow(), dispatcher.render() signatures all changed.
        // Use simplified rendering approach.
        final EntityRenderDispatcher dispatcher = mc.getEntityRenderDispatcher();
        pitchRotation.conjugate();
        // TODO: dispatcher.overrideCameraOrientation() removed in 1.21.11
        //  Entity rendering API changed significantly — render() signature differs.
        //  Stubbed out until full port to new rendering pipeline.
        final MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        // 1.21.11: EntityRenderDispatcher.render() and EntityRenderer.render() signatures
        // changed to use a state-object pattern. Direct entity rendering in inventory/GUI
        // contexts requires the new InventoryScreen.renderEntityInInventory() or similar.
        // Stubbed out — callers should handle the case where entity is not rendered.
        buffers.endBatch();
        entity.setYRot(oldYaw);
        entity.setXRot(oldPitch);
        if (livingEntity != null)
        {
            livingEntity.yBodyRot = oldYawOffset;
            livingEntity.yHeadRotO = oldPrevYawHead;
            livingEntity.yHeadRot = oldYawHead;
        }
        poseStack.popPose();
    }

    /**
     * @return rendering lambda detached from sprite and guiScaling instances
     */
    public static ResolvedBlit resolveSprite(final TextureAtlasSprite sprite, final GuiSpriteScaling guiScaling)
    {
        final Identifier atlasLocation = sprite.atlasLocation();
        final float u0 = sprite.getU0();
        final float v0 = sprite.getV0();
        final float u1 = sprite.getU1();
        final float v1 = sprite.getV1();
        if (guiScaling.type() == Type.STRETCH)
        {
            return (ps, x, y, w, h) -> blit(ps, atlasLocation, x, y, w, h, u0, v0, u1, v1);
        }
        else if (guiScaling instanceof final NineSlice nineSlice)
        {
            final int rbW = nineSlice.width();
            final int rbH = nineSlice.height();
            final int uR = nineSlice.border().left();
            final int vR = nineSlice.border().top();
            final int rW = rbW - uR - nineSlice.border().right();
            final int rH = rbH - vR - nineSlice.border().bottom();

            return (ps, x, y, w, h) -> {
                if (rbW == w && rbH == h)
                {
                    blit(ps, atlasLocation, x, y, w, h, u0, v0, u1, v1);
                }
                else
                {
                    blitRepeatable(ps, atlasLocation, x, y, w, h, u0, v0, u1, v1, uR, vR, rW, rH, rbW, rbH);
                }
            };
        }
        else if (guiScaling instanceof final Tile tile)
        {
            final int tW = tile.width();
            final int tH = tile.height();

            return (ps, x, y, w, h) -> {
                if (tW == w && tH == h)
                {
                    blit(ps, atlasLocation, x, y, w, h, u0, v0, u1, v1);
                }
                else
                {
                    blitRepeatable(ps, atlasLocation, x, y, w, h, u0, v0, u1, v1, 0, 0, tW, tH, tW, tH);
                }
            };
        }
        return ResolvedBlit.EMPTY;
    }

    /**
     * Used for precompiling math around rendering
     */
    @FunctionalInterface
    public static interface ResolvedBlit
    {
        public static final ResolvedBlit EMPTY = (ps, x, y, w, h) -> {};

        void blit(PoseStack ps, int x, int y, int w, int h);
    }
}
