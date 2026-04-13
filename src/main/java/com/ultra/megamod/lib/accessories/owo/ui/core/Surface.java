package com.ultra.megamod.lib.accessories.owo.ui.core;

/**
 * Adapter stub for io.wispforest.owo.ui.core.Surface.
 * Not a functional interface to avoid ambiguity with SurfaceRenderer.
 */
public abstract class Surface {
    public abstract void draw(OwoUIDrawContext context, Component component);

    public static final Surface VANILLA_TRANSLUCENT = new Surface() {
        @Override public void draw(OwoUIDrawContext context, Component component) {}
    };

    public static final Surface BLANK = new Surface() {
        @Override public void draw(OwoUIDrawContext context, Component component) {}
    };

    public static Surface optionsBackground() {
        return BLANK;
    }

    public Surface and(Surface other) {
        var self = this;
        return new Surface() {
            @Override public void draw(OwoUIDrawContext context, Component component) {
                self.draw(context, component);
                other.draw(context, component);
            }
        };
    }

    public static Surface flat(int color) {
        return BLANK;
    }
}
