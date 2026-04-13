package com.ultra.megamod.lib.accessories.owo.ui.core;

/**
 * Adapter stub for io.wispforest.owo.ui.core.Positioning.
 */
public record Positioning(int x, int y, Type type) {

    public enum Type {
        LAYOUT, ABSOLUTE, RELATIVE, ACROSS
    }

    public static Positioning absolute(int x, int y) { return new Positioning(x, y, Type.ABSOLUTE); }
    public static Positioning relative(int x, int y) { return new Positioning(x, y, Type.RELATIVE); }
    public static Positioning across(int x, int y) { return new Positioning(x, y, Type.ACROSS); }
    public static Positioning layout() { return new Positioning(0, 0, Type.LAYOUT); }
}
