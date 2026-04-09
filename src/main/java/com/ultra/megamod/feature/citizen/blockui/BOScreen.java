package com.ultra.megamod.feature.citizen.blockui;

import com.ultra.megamod.feature.citizen.blockui.util.cursor.CursorUtils;
import com.ultra.megamod.feature.citizen.blockui.views.BOWindow;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;

/**
 * Wraps MineCrafts GuiScreen for BlockOut's Window.
 * Adapted for MC 1.21.11 rendering API changes.
 */
public class BOScreen extends Screen
{
    protected double renderScale = 1.0d;
    protected double mcScale = 1.0d;
    protected BOWindow window;
    protected double x = 0;
    protected double y = 0;
    public static boolean isMouseLeftDown = false;
    protected boolean isOpen = false;
    protected int framebufferWidth;
    protected int framebufferHeight;
    protected int absoluteMouseX;
    protected int absoluteMouseY;

    /**
     * Create a GuiScreen from a BlockOut window.
     *
     * @param w blockout window.
     */
    public BOScreen(final BOWindow w)
    {
        super(Component.literal("MegaMod GUI"));
        window = w;
    }

    @Override
    public void render(final GuiGraphics ms, final int mx, final int my, final float f)
    {
        if (minecraft == null || !isOpen) // should never happen though
        {
            return;
        }

        absoluteMouseX = mx;
        absoluteMouseY = my;
        framebufferWidth = minecraft.getWindow().getWidth();
        framebufferHeight = minecraft.getWindow().getHeight();
        final int guiWidth = Math.max(framebufferWidth, 320);
        final int guiHeight = Math.max(framebufferHeight, 240);

        mcScale = minecraft.getWindow().getGuiScale();
        renderScale = window.getRenderType().calcRenderScale(minecraft.getWindow(), window);

        if (window.hasLightbox() && minecraft.screen == this)
        {
            // Lightbox covers full screen in GUI-scaled coordinates
            final int screenW = (int) Math.ceil(framebufferWidth / mcScale);
            final int screenH = (int) Math.ceil(framebufferHeight / mcScale);
            ms.fillGradient(0, 0, screenW, screenH, -1072689136, -804253680);
        }

        width = window.getWidth();
        height = window.getHeight();
        // Compute centering position in framebuffer pixel coordinates
        x = Math.floor((guiWidth - width * renderScale) / 2.0d);
        y = Math.floor((guiHeight - height * renderScale) / 2.0d);

        // The vanilla GuiGraphics operates in GUI-scaled coordinates (framebuffer pixels / guiScale).
        // We need the BlockOut PoseStack to transform from BlockOut window coords to GUI-scaled coords.
        // x,y are in framebuffer pixels, so divide by mcScale to get GUI-scaled position.
        // renderScale is relative to framebuffer pixels, so also divide by mcScale to get GUI-scaled scaling.
        final float guiScaledX = (float) (x / mcScale);
        final float guiScaledY = (float) (y / mcScale);
        final float guiScaledRenderScale = (float) (renderScale / mcScale);

        final PoseStack newMs = new PoseStack();
        newMs.translate(guiScaledX, guiScaledY, 0.0f);
        newMs.scale(guiScaledRenderScale, guiScaledRenderScale, 1.0f);

        // Provide GuiGraphics and the BlockOut PoseStack to UiRenderMacros for rendering
        UiRenderMacros.setCurrentGuiGraphics(ms, newMs);

        try
        {
            final BOGuiGraphics target = new BOGuiGraphics(minecraft, newMs, ms);
            window.draw(target, calcRelativeX(mx), calcRelativeY(my));

            if (minecraft.screen == this)
            {
                int debugX = (int) (-x / renderScale) + 3;
                if (Pane.debugging)
                {
                    debugX = target.drawString(
                        "XML: %s Scaling: %s (vanilla: %.2f our: %.2f) "
                            .formatted(window.getXmlResourceLocation(), window.getRenderType().name(), mcScale, renderScale),
                        debugX,
                        -minecraft.font.lineHeight,
                        Color.getByName("white"));
                }
                target.applyCursor(debugX);
            }

            window.drawLast(target, calcRelativeX(mx), calcRelativeY(my));
        }
        catch (final Exception e)
        {
            final CrashReport crashReport = CrashReport.forThrowable(e, "Rendering BO screen");
            final CrashReportCategory category = crashReport.addCategory("BO screen rendering details");
            category.setDetail("XML res loc", () -> window.getXmlResourceLocation().toString());
            category.setDetail("Scaling mode (window render type)", () -> window.getRenderType().name());
            category.setDetail("Vanilla gui scale", () -> Double.toString(mcScale));
            category.setDetail("BO gui scale", () -> Double.toString(renderScale));
            throw new ReportedException(crashReport);
        }
    }

    // No @Override per CLAUDE.md - signatures changed in 1.21.11
    public boolean keyPressed(final int key, final int scanCode, final int modifiers)
    {
        // keys without printable representation
        if (key >= 0 && key <= GLFW.GLFW_KEY_LAST)
        {
            try
            {
                return window.onKeyTyped('\0', key);
            }
            catch (final Exception e)
            {
                final CrashReport crashReport = CrashReport.forThrowable(e, "KeyPressed event for BO screen");
                final CrashReportCategory category = crashReport.addCategory("BO screen key event details");
                category.setDetail("XML res loc", () -> window.getXmlResourceLocation().toString());
                category.setDetail("GLFW key value", () -> Integer.toString(key));
                throw new ReportedException(crashReport);
            }
        }
        return false;
    }

    // No @Override per CLAUDE.md - signatures changed in 1.21.11
    public boolean charTyped(final char ch, final int key)
    {
        try
        {
            return window.onKeyTyped(ch, key);
        }
        catch (final Exception e)
        {
            final CrashReport crashReport = CrashReport.forThrowable(e, "CharTyped event for BO screen");
            final CrashReportCategory category = crashReport.addCategory("BO screen char event details");
            category.setDetail("XML res loc", () -> window.getXmlResourceLocation().toString());
            category.setDetail("Char value", () -> Character.toString(ch));
            throw new ReportedException(crashReport);
        }
    }

    // No @Override per CLAUDE.md - signatures changed in 1.21.11
    public boolean mouseClicked(final double mxIn, final double myIn, final int keyCode)
    {
        final double mx = calcRelativeX(mxIn);
        final double my = calcRelativeY(myIn);
        try
        {
            if (keyCode == GLFW.GLFW_MOUSE_BUTTON_LEFT)
            {
                // Adjust coordinate to origin of window
                isMouseLeftDown = true;
                return window.click(mx, my);
            }
            else if (keyCode == GLFW.GLFW_MOUSE_BUTTON_RIGHT)
            {
                return window.rightClick(mx, my);
            }
        }
        catch (final Exception e)
        {
            final CrashReport crashReport = CrashReport.forThrowable(e, "MousePressed event for BO screen");
            final CrashReportCategory category = crashReport.addCategory("BO screen mouse event details");
            category.setDetail("XML res loc", () -> Objects.toString(window.getXmlResourceLocation()));
            category.setDetail("GLFW mouse key value", () -> Integer.toString(keyCode));
            throw new ReportedException(crashReport);
        }
        return false;
    }

    // No @Override per CLAUDE.md - signatures changed in 1.21.11
    public boolean mouseScrolled(final double mx, final double my, final double scrollHorizontalDiff, final double scrollVerticalDiff)
    {
        if (scrollVerticalDiff != 0)
        {
            try
            {
                return window.scrollInput(scrollHorizontalDiff * 10, scrollVerticalDiff * 10, calcRelativeX(mx), calcRelativeY(my));
            }
            catch (final Exception e)
            {
                final CrashReport crashReport = CrashReport.forThrowable(e, "MouseScroll event for BO screen");
                final CrashReportCategory category = crashReport.addCategory("BO screen scroll event details");
                category.setDetail("XML res loc", () -> window.getXmlResourceLocation().toString());
                category.setDetail("Scroll value", () -> Double.toString(scrollVerticalDiff));
                throw new ReportedException(crashReport);
            }
        }
        return false;
    }

    // No @Override per CLAUDE.md - signatures changed in 1.21.11
    public boolean mouseDragged(final double xIn, final double yIn, final int speed, final double deltaX, final double deltaY)
    {
        try
        {
            return window.onMouseDrag(calcRelativeX(xIn), calcRelativeY(yIn), speed, deltaX, deltaY);
        }
        catch (final Exception e)
        {
            final CrashReport crashReport = CrashReport.forThrowable(e, "MouseDragged event for BO screen");
            final CrashReportCategory category = crashReport.addCategory("BO screen mouse event details");
            category.setDetail("XML res loc", () -> window.getXmlResourceLocation().toString());
            throw new ReportedException(crashReport);
        }
    }

    // No @Override per CLAUDE.md - signatures changed in 1.21.11
    public boolean mouseReleased(final double mxIn, final double myIn, final int keyCode)
    {
        if (keyCode == GLFW.GLFW_MOUSE_BUTTON_LEFT)
        {
            // Adjust coordinate to origin of window
            isMouseLeftDown = false;
            try
            {
                return window.onMouseReleased(calcRelativeX(mxIn), calcRelativeY(myIn));
            }
            catch (final Exception e)
            {
                final CrashReport crashReport = CrashReport.forThrowable(e, "MouseReleased event for BO screen");
                final CrashReportCategory category = crashReport.addCategory("BO screen mouse event details");
                category.setDetail("XML res loc", () -> window.getXmlResourceLocation().toString());
                category.setDetail("GLFW mouse key value", () -> Integer.toString(keyCode));
                throw new ReportedException(crashReport);
            }
        }
        return false;
    }

    /**
     * Get the open window here.
     * @return the window.
     */
    public BOWindow getWindow()
    {
        return window;
    }

    @Override
    public void tick()
    {
        try
        {
            if (minecraft != null)
            {
                if (!isOpen)
                {
                    window.onOpened();
                    isOpen = true;
                }
                else
                {
                    window.onUpdate();

                    final LocalPlayer player = minecraft == null ? null : minecraft.player;
                    if (player != null && !player.isAlive())
                    {
                        player.closeContainer();
                    }
                }
            }
        }
        catch (final Exception e)
        {
            final CrashReport crashReport = CrashReport.forThrowable(e, "Ticking/Updating BO screen");
            final CrashReportCategory category = crashReport.addCategory("BO screen update details");
            category.setDetail("XML res loc", () -> window.getXmlResourceLocation().toString());
            category.setDetail("Is opened", () -> Boolean.toString(isOpen));
            throw new ReportedException(crashReport);
        }
    }

    @Override
    public void removed()
    {
        try
        {
            window.onClosed();
        }
        catch (final Exception e)
        {
            final CrashReport crashReport = CrashReport.forThrowable(e, "Closing BO screen");
            final CrashReportCategory category = crashReport.addCategory("BO screen closing details");
            category.setDetail("XML res loc", () -> window.getXmlResourceLocation().toString());
            category.setDetail("Is opened", () -> Boolean.toString(isOpen));
            throw new ReportedException(crashReport);
        }
        finally
        {
            BOWindow.clearFocus();
            CursorUtils.resetCursor();
        }
    }

    @Override
    public boolean isPauseScreen()
    {
        return window.doesWindowPauseGame();
    }

    /**
     * Converts X from event to unscaled and unscrolled X for child in relative (top-left) coordinates.
     */
    private double calcRelativeX(final double xIn)
    {
        return (xIn * mcScale - x) / renderScale;
    }

    /**
     * Converts Y from event to unscaled and unscrolled Y for child in relative (top-left) coordinates.
     */
    private double calcRelativeY(final double yIn)
    {
        return (yIn * mcScale - y) / renderScale;
    }

    public double getRenderScale()
    {
        return renderScale;
    }

    public double getVanillaGuiScale()
    {
        return mcScale;
    }

    public int getFramebufferWidth()
    {
        return framebufferWidth;
    }

    public int getFramebufferHeight()
    {
        return framebufferHeight;
    }

    public int getAbsoluteMouseX()
    {
        return absoluteMouseX;
    }

    public int getAbsoluteMouseY()
    {
        return absoluteMouseY;
    }
}
