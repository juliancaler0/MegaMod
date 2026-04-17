package com.ultra.megamod.lib.accessories.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

/**
 * Direct GuiGraphics nine-patch blitter. Draws a 9-region texture so a
 * fixed-size border is preserved and the center patch stretches to fit
 * arbitrary panel sizes. Ports the math from
 * {@code com.ultra.megamod.lib.owo.ui.util.NinePatchTexture} against the
 * vanilla GuiGraphics API so the accessories screen can use real owo
 * panel chrome without going through the full owo pipeline.
 */
public final class NinePatchHelper {

    private NinePatchHelper() {}

    /**
     * Blit a nine-patch.
     *
     * @param g             render target
     * @param texture       texture id (e.g. accessories:textures/gui/accessories_panel.png)
     * @param x,y           top-left corner on screen
     * @param w,h           on-screen panel size
     * @param u,v           top-left corner in the texture
     * @param cornerSize    size of each corner patch (usually 4 or 5)
     * @param centerSize    size of the center patch that gets stretched
     * @param textureW,textureH total atlas size (for UV math — typically 256)
     */
    public static void draw(GuiGraphics g, Identifier texture,
                            int x, int y, int w, int h,
                            int u, int v,
                            int cornerSize, int centerSize,
                            int textureW, int textureH) {
        var pipe = RenderPipelines.GUI_TEXTURED;
        int cw = cornerSize, ch = cornerSize;
        int mw = centerSize, mh = centerSize;
        int rightEdge = cw + mw;
        int bottomEdge = ch + mh;
        int doubleW = cw * 2;
        int doubleH = ch * 2;

        // Corners
        g.blit(pipe, texture, x,             y,             u,           v,          cw, ch, cw, ch, textureW, textureH);
        g.blit(pipe, texture, x + w - cw,   y,             u + rightEdge, v,         cw, ch, cw, ch, textureW, textureH);
        g.blit(pipe, texture, x,             y + h - ch,   u,           v + bottomEdge, cw, ch, cw, ch, textureW, textureH);
        g.blit(pipe, texture, x + w - cw,   y + h - ch,   u + rightEdge, v + bottomEdge, cw, ch, cw, ch, textureW, textureH);

        // Center
        if (w > doubleW && h > doubleH) {
            g.blit(pipe, texture, x + cw, y + ch, u + cw, v + ch,
                    w - doubleW, h - doubleH, mw, mh, textureW, textureH);
        }

        // Top + bottom edges
        if (w > doubleW) {
            g.blit(pipe, texture, x + cw, y,           u + cw, v,
                    w - doubleW, ch, mw, ch, textureW, textureH);
            g.blit(pipe, texture, x + cw, y + h - ch, u + cw, v + bottomEdge,
                    w - doubleW, ch, mw, ch, textureW, textureH);
        }

        // Left + right edges
        if (h > doubleH) {
            g.blit(pipe, texture, x,           y + ch, u,             v + ch,
                    cw, h - doubleH, cw, mh, textureW, textureH);
            g.blit(pipe, texture, x + w - cw, y + ch, u + rightEdge, v + ch,
                    cw, h - doubleH, cw, mh, textureW, textureH);
        }
    }

    /** Convenience overload with standard accessories_panel layout (5px corners, 5px center, 256×256 atlas). */
    public static void drawPanel(GuiGraphics g, Identifier texture, int x, int y, int w, int h) {
        draw(g, texture, x, y, w, h, 0, 0, 5, 5, 256, 256);
    }

    /** 18×18 slot_frame ornamentation overlaid on a single 18×18 slot. */
    public static void drawSlotFrame(GuiGraphics g, Identifier texture, int slotX, int slotY) {
        g.blit(RenderPipelines.GUI_TEXTURED, texture, slotX - 1, slotY - 1, 0, 0, 18, 18, 18, 18);
    }
}
