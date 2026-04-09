package com.ultra.megamod.feature.citizen.blockui;

import com.ultra.megamod.feature.citizen.blockui.util.cursor.Cursor;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.Nullable;

/**
 * Wrapper around GuiGraphics for BlockOut's rendering system.
 * Adapted for 1.21.11 where GuiGraphics no longer has a public PoseStack-based constructor.
 * Instead of extending GuiGraphics, this wraps a GuiGraphics instance and a separate PoseStack.
 */
public class BOGuiGraphics
{
    private int cursorMaxDepth = -1;
    private Cursor selectedCursor = Cursor.DEFAULT;

    private final Minecraft mc;
    private final PoseStack poseStack;
    private final GuiGraphics wrapped;

    /**
     * Create a BOGuiGraphics with the given PoseStack for BlockOut rendering.
     *
     * @param mc        minecraft instance
     * @param ps        the PoseStack for BlockOut coordinate transforms
     * @param wrapped   the vanilla GuiGraphics from the render method (used for item rendering, buffers, etc.)
     */
    public BOGuiGraphics(final Minecraft mc, final PoseStack ps, final GuiGraphics wrapped)
    {
        this.mc = mc;
        this.poseStack = ps;
        this.wrapped = wrapped;
    }

    /**
     * Returns the BlockOut PoseStack used for positioning.
     */
    public PoseStack pose()
    {
        return poseStack;
    }

    /**
     * Returns the buffer source from the wrapped GuiGraphics.
     */
    public BufferSource bufferSource()
    {
        return mc.renderBuffers().bufferSource();
    }

    private Font getFont(@Nullable final ItemStack itemStack)
    {
        if (itemStack != null)
        {
            final Font font = IClientItemExtensions.of(itemStack).getFont(itemStack, IClientItemExtensions.FontContext.ITEM_COUNT);
            if (font != null)
            {
                return font;
            }
        }
        return mc.font;
    }

    public void renderItemDecorations(final ItemStack itemStack, final int x, final int y)
    {
        pushBlockoutTransform();
        wrapped.renderItemDecorations(getFont(itemStack), itemStack, x, y);
        popBlockoutTransform();
    }

    public void renderItemDecorations(final ItemStack itemStack, final int x, final int y, @Nullable final String altStackSize)
    {
        pushBlockoutTransform();
        wrapped.renderItemDecorations(getFont(itemStack), itemStack, x, y, altStackSize);
        popBlockoutTransform();
    }

    public void renderItem(final ItemStack itemStack, final int x, final int y)
    {
        pushBlockoutTransform();
        wrapped.renderItem(itemStack, x, y);
        popBlockoutTransform();
    }

    /**
     * Push the BlockOut transforms onto the vanilla GuiGraphics pose (Matrix3x2fStack in 1.21.11).
     * Extracts 2D translation and scale from the BlockOut 4x4 matrix.
     */
    private void pushBlockoutTransform()
    {
        final org.joml.Matrix3x2fStack guiPose = wrapped.pose();
        guiPose.pushMatrix();
        final org.joml.Matrix4f m = poseStack.last().pose();
        guiPose.translate(m.m30(), m.m31());
        guiPose.scale(m.m00(), m.m11());
    }

    /**
     * Pop the BlockOut transforms from the vanilla GuiGraphics pose.
     */
    private void popBlockoutTransform()
    {
        wrapped.pose().popMatrix();
    }

    public int drawString(final String text, final float x, final float y, final int color)
    {
        return drawString(text, x, y, color, false);
    }

    public int drawString(final String text, final float x, final float y, final int color, final boolean shadow)
    {
        // Ensure alpha is set - MC 1.21.11 Font.drawInBatch treats 0 alpha as fully transparent
        final int safeColor = (color & 0xFF000000) == 0 ? (color | 0xFF000000) : color;
        // Use font.drawInBatch with the BlockOut PoseStack matrix so coordinates are correct
        final org.joml.Matrix4f matrix = poseStack.last().pose();
        final net.minecraft.client.renderer.MultiBufferSource.BufferSource bufferSource = bufferSource();
        mc.font.drawInBatch(text, x, y, safeColor, shadow, matrix, bufferSource, net.minecraft.client.gui.Font.DisplayMode.NORMAL, 0, 15728880);
        bufferSource.endBatch();
        return (int) x + mc.font.width(text);
    }

    public void setCursor(final Cursor cursor)
    {
        // PoseStack.size() was removed in 1.21.11; track depth via push/pop count instead
        // For simplicity, always update cursor (last one wins per frame)
        selectedCursor = cursor;
    }

    /**
     * @param debugXoffset debug string x offset
     */
    public void applyCursor(final int debugXoffset)
    {
        selectedCursor.apply();

        if (Pane.debugging)
        {
            drawString(selectedCursor.toString(), debugXoffset, -mc.font.lineHeight, Color.getByName("white"));
        }
    }

    public void pushMvApplyPose()
    {
        // In 1.21.11, model view stack operations are simplified
        // The pose stack handles transforms directly
    }

    public void popMvPose()
    {
        // In 1.21.11, model view stack operations are simplified
    }

    public static double getAltSpeedFactor()
    {
        // Check if Alt key is held using InputConstants
        long windowHandle = 0;
        try {
            var window = Minecraft.getInstance().getWindow();
            var field = window.getClass().getDeclaredField("window");
            field.setAccessible(true);
            windowHandle = field.getLong(window);
        } catch (Exception e) {
            return 1;
        }
        return org.lwjgl.glfw.GLFW.glfwGetKey(windowHandle,
            org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_ALT) == org.lwjgl.glfw.GLFW.GLFW_PRESS ? 5 : 1;
    }
}
