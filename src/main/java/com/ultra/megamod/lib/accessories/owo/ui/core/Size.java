package com.ultra.megamod.lib.accessories.owo.ui.core;

/**
 * Adapter stub for io.wispforest.owo.ui.core.Size.
 */
public record Size(int width, int height) {
    public static Size of(int width, int height) { return new Size(width, height); }
    public static Size square(int size) { return new Size(size, size); }
}
