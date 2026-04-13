package com.ultra.megamod.lib.accessories.owo.ui.core;

/**
 * Adapter stub for io.wispforest.owo.ui.core.PositionedRectangle.
 */
public interface PositionedRectangle {
    int x();
    int y();
    int width();
    int height();

    default boolean isInBoundingBox(double px, double py) {
        return px >= x() && px < x() + width() && py >= y() && py < y() + height();
    }

    static PositionedRectangle of(int x, int y, int width, int height) {
        return new PositionedRectangle() {
            @Override public int x() { return x; }
            @Override public int y() { return y; }
            @Override public int width() { return width; }
            @Override public int height() { return height; }
        };
    }
}
