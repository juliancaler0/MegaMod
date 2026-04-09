package com.ultra.megamod.feature.citizen.blockui.util.cursor;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class to manage cursor image.
 * Simplified from BlockUI - removed custom cursor texture support, kept standard cursors.
 */
public class CursorUtils
{
    private static final Logger LOGGER = LoggerFactory.getLogger(CursorUtils.class);
    private static final long[] STANDARD_CURSORS = new long[StandardCursor.values().length];
    private static long lastCursorAddress = 0;

    /**
     * Sets cursor image to standard shapes provided by GLFW.
     *
     * @param shape cursor shape
     */
    public static void setStandardCursor(final StandardCursor shape)
    {
        if (shape == StandardCursor.DEFAULT)
        {
            resetCursor();
            return;
        }

        final int ord = shape.ordinal();

        if (STANDARD_CURSORS[ord] == 0)
        {
            RenderSystem.assertOnRenderThread();
            STANDARD_CURSORS[ord] = GLFW.glfwCreateStandardCursor(shape.glfwValue);
            if (STANDARD_CURSORS[ord] == 0)
            {
                LOGGER.error("Cannot create standard cursor for shape: " + shape);
                return;
            }
        }

        setCursorAddress(STANDARD_CURSORS[ord]);
    }

    /**
     * Sets cursor image to default (usually arrow).
     */
    public static void resetCursor()
    {
        setCursorAddress(0);
    }

    /**
     * Sets cursor image address. If null (zero), cursor is reset to default (usually arrow).
     *
     * @param cursorAddress cursor handle address or null
     */
    public static void setCursorAddress(final long cursorAddress)
    {
        RenderSystem.assertOnRenderThread();
        if (cursorAddress != lastCursorAddress)
        {
            try
            {
                // In 1.21.11, Window.getWindow() is removed. Use reflection to get GLFW handle.
                var window = Minecraft.getInstance().getWindow();
                // Try common method names for the GLFW window handle
                long handle = 0;
                try {
                    var method = window.getClass().getMethod("getWindow");
                    handle = (long) method.invoke(window);
                } catch (NoSuchMethodException e1) {
                    try {
                        // Fallback: try field access
                        var field = window.getClass().getDeclaredField("window");
                        field.setAccessible(true);
                        handle = field.getLong(window);
                    } catch (Exception e2) {
                        // Last resort: skip cursor change
                        return;
                    }
                }
                GLFW.glfwSetCursor(handle, cursorAddress);
                lastCursorAddress = cursorAddress;
            }
            catch (final Exception e)
            {
                // Silently ignore cursor setting failures
            }
        }
    }

    /**
     * @param  testedAddress param of tested cursor handle
     * @return               true if given address is equal to current cursor handle address
     */
    public static boolean isCurrentCursor(final long testedAddress)
    {
        return testedAddress == lastCursorAddress;
    }

    /**
     * Enum representing all possible cursors defined by GLFW
     */
    public enum StandardCursor
    {
        DEFAULT(Integer.MIN_VALUE),
        ARROW(GLFW.GLFW_ARROW_CURSOR),
        TEXT_CURSOR(GLFW.GLFW_IBEAM_CURSOR),
        CROSSHAIR(GLFW.GLFW_CROSSHAIR_CURSOR),
        HAND(GLFW.GLFW_POINTING_HAND_CURSOR),
        HORIZONTAL_RESIZE(GLFW.GLFW_RESIZE_EW_CURSOR),
        VERTICAL_RESIZE(GLFW.GLFW_RESIZE_NS_CURSOR),
        RESIZE(GLFW.GLFW_RESIZE_ALL_CURSOR),

        /** unsafe */
        RESIZE2(GLFW.GLFW_RESIZE_NWSE_CURSOR),
        /** unsafe */
        RESIZE3(GLFW.GLFW_RESIZE_NESW_CURSOR);

        private final int glfwValue;

        private StandardCursor(final int glfwValue)
        {
            this.glfwValue = glfwValue;
        }
    }
}
