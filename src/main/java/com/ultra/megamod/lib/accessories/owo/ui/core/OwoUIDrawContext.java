package com.ultra.megamod.lib.accessories.owo.ui.core;

import net.minecraft.client.gui.GuiGraphics;

/**
 * Adapter stub for io.wispforest.owo.ui.core.OwoUIDrawContext.
 * Wraps GuiGraphics for extended drawing operations.
 */
public class OwoUIDrawContext {
    private final GuiGraphics guiGraphics;

    public OwoUIDrawContext(GuiGraphics guiGraphics) {
        this.guiGraphics = guiGraphics;
    }

    public GuiGraphics getGraphics() { return guiGraphics; }

    // Convenience delegate methods - no-ops in stub
    public OwoUIDrawContext push() { return this; }
    public OwoUIDrawContext pop() { return this; }
    public OwoUIDrawContext translate(float x, float y) { return this; }
    public OwoUIDrawContext scale(float sx, float sy) { return this; }

    public void enableScissor(int x, int y, int width, int height) {
        if (guiGraphics != null) guiGraphics.enableScissor(x, y, x + width, y + height);
    }

    public void disableScissor() {
        if (guiGraphics != null) guiGraphics.disableScissor();
    }

    public void fill(int x1, int y1, int x2, int y2, int color) {
        if (guiGraphics != null) guiGraphics.fill(x1, y1, x2, y2, color);
    }

    /**
     * Returns a matrix stack for transformations.
     * Stub - returns a dummy that has no-op rotateAbout.
     */
    public MatrixStackWrapper getMatrixStack() {
        return new MatrixStackWrapper();
    }

    public static class MatrixStackWrapper {
        public MatrixStackWrapper rotateAbout(float angle, float x, float y) { return this; }
        public MatrixStackWrapper translate(float x, float y, float z) { return this; }
        public MatrixStackWrapper scale(float sx, float sy, float sz) { return this; }
    }

    public static OwoUIDrawContext wrap(GuiGraphics guiGraphics) {
        return new OwoUIDrawContext(guiGraphics);
    }
}
