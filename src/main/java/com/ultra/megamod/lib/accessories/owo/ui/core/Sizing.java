package com.ultra.megamod.lib.accessories.owo.ui.core;

/**
 * Adapter stub for io.wispforest.owo.ui.core.Sizing.
 */
public class Sizing {
    public final int value;

    private Sizing() { this.value = 0; }
    private Sizing(int value) { this.value = value; }

    public int get() { return value; }

    public static Sizing content() { return new Sizing(); }
    public static Sizing fixed(int value) { return new Sizing(value); }
    public static Sizing fill() { return new Sizing(); }
    public static Sizing expand() { return new Sizing(); }
    public static Sizing expand(int percentage) { return new Sizing(percentage); }

    public float contentFactor() { return 1.0f; }

    public boolean isContent() { return value == 0; }
}
