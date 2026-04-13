package com.ultra.megamod.lib.accessories.owo.ui.core;

/**
 * Adapter stub for io.wispforest.owo.ui.core.Color.
 */
public record Color(float red, float green, float blue, float alpha) {
    public Color(float r, float g, float b) {
        this(r, g, b, 1.0f);
    }

    public int argb() {
        int a = (int) (alpha * 255) & 0xFF;
        int r = (int) (red * 255) & 0xFF;
        int g = (int) (green * 255) & 0xFF;
        int b = (int) (blue * 255) & 0xFF;
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public static final Color WHITE = new Color(1f, 1f, 1f, 1f);
    public static final Color BLACK = new Color(0f, 0f, 0f, 1f);

    public static Color ofFormatting(net.minecraft.ChatFormatting formatting) {
        return WHITE; // Stub
    }

    public static Color ofArgb(int argb) {
        float a = ((argb >> 24) & 0xFF) / 255.0f;
        float r = ((argb >> 16) & 0xFF) / 255.0f;
        float g = ((argb >> 8) & 0xFF) / 255.0f;
        float b = (argb & 0xFF) / 255.0f;
        return new Color(r, g, b, a);
    }
}
