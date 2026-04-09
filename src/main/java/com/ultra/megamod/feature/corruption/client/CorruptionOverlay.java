package com.ultra.megamod.feature.corruption.client;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;

/**
 * Client-side GUI layer overlay displayed when the player is in a corrupted chunk.
 * Shows a dark purple vignette around screen edges, with intensity scaling by corruption strength.
 * Also displays a "Corrupted Zone (Strength X)" indicator in the top-right corner.
 *
 * Corruption strength is tracked client-side via the CorruptionClientTracker.
 */
public class CorruptionOverlay {

    // Pulsing parameters
    private static final float PULSE_MIN = 0.08f;
    private static final float PULSE_AMPLITUDE = 0.10f;
    private static final double PULSE_SPEED = 200.0; // ms per pulse cycle component

    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(Identifier.fromNamespaceAndPath("megamod", "corruption_overlay"),
                CorruptionOverlay::render);
    }

    private static void render(GuiGraphics g, DeltaTracker dt) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) return;

        // Get corruption strength from client-side tracker
        int strength = CorruptionClientTracker.getCurrentStrength();
        if (strength <= 0) return;

        // Intensity: scales with tier (0.3 at tier 1, up to 1.0 at tier 4)
        float intensity = Math.min(1.0f, 0.15f + strength * 0.2125f);

        // Pulsing effect — sinusoidal oscillation over time
        float pulse = PULSE_MIN + PULSE_AMPLITUDE * (float) Math.sin(System.currentTimeMillis() / PULSE_SPEED);
        float baseAlpha = pulse * intensity;

        int w = g.guiWidth();
        int h = g.guiHeight();
        int strips = 40;

        // Draw dark purple gradient vignette along all 4 edges
        for (int i = 0; i < strips; i++) {
            float fade = 1.0f - (float) i / strips;
            fade = fade * fade; // Quadratic falloff for natural gradient
            int alpha = (int) (baseAlpha * fade * 255) & 0xFF;
            if (alpha < 2) continue;
            // Dark purple color: #440066
            int color = (alpha << 24) | 0x440066;

            // Left edge
            g.fill(i, 0, i + 1, h, color);
            // Right edge
            g.fill(w - i - 1, 0, w - i, h, color);
            // Top edge
            g.fill(0, i, w, i + 1, color);
            // Bottom edge
            g.fill(0, h - i - 1, w, h - i, color);
        }

        // Draw corruption indicator in top-right corner
        drawCorruptionIndicator(g, w, strength);
    }

    /**
     * Draws a small "Corrupted Zone (Strength X)" label in the top-right.
     */
    private static void drawCorruptionIndicator(GuiGraphics g, int screenWidth, int strength) {
        Minecraft mc = Minecraft.getInstance();
        String text = "\u2620 Corrupted Zone (Tier " + strength + ")"; // skull emoji
        int textWidth = mc.font.width(text);
        int x = screenWidth - textWidth - 6;
        int y = 6;

        // Semi-transparent background
        int bgAlpha = 160;
        int bgColor = (bgAlpha << 24) | 0x1A001A;
        g.fill(x - 4, y - 2, x + textWidth + 4, y + 12, bgColor);

        // Border
        int borderColor = (200 << 24) | 0x7700AA;
        g.fill(x - 5, y - 3, x + textWidth + 5, y - 2, borderColor); // top
        g.fill(x - 5, y + 12, x + textWidth + 5, y + 13, borderColor); // bottom
        g.fill(x - 5, y - 2, x - 4, y + 12, borderColor); // left
        g.fill(x + textWidth + 4, y - 2, x + textWidth + 5, y + 12, borderColor); // right

        // Pulsing text color
        float pulse = 0.7f + 0.3f * (float) Math.sin(System.currentTimeMillis() / 300.0);
        int red = (int)(pulse * 170);
        int green = 0;
        int blue = (int)(pulse * 220);
        int textColor = 0xFF000000 | (red << 16) | (green << 8) | blue;

        g.drawString(mc.font, text, x, y, textColor, true);
    }
}
