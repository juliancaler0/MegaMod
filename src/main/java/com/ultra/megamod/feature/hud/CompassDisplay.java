package com.ultra.megamod.feature.hud;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;

/**
 * Minimal compass bar at the top center of the screen.
 * Shows cardinal and intercardinal directions with degree tick marks.
 * No background — just text that fades from bright (center) to dim (edges).
 */
public class CompassDisplay {

    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(Identifier.fromNamespaceAndPath("megamod", "compass"),
            CompassDisplay::render);
    }

    // Cardinal/intercardinal directions and their yaw angles
    private static final String[] LABELS = {"S", "SW", "W", "NW", "N", "NE", "E", "SE"};
    private static final float[] LABEL_YAWS = {0, 45, 90, 135, 180, 225, 270, 315};
    private static final int[] LABEL_COLORS = {
        0xFFFF5555, // S - red
        0xFF888899, // SW
        0xFFCCCCDD, // W
        0xFF888899, // NW
        0xFF5555FF, // N - blue
        0xFF888899, // NE
        0xFFCCCCDD, // E
        0xFF888899, // SE
    };

    // Visible width of the compass bar in pixels
    private static final int BAR_HALF_WIDTH = 120;
    // Degrees visible across the full bar width
    private static final float VISIBLE_DEGREES = 180f;

    private static void render(GuiGraphics g, DeltaTracker dt) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null || mc.options.hideGui) return;

        int screenW = g.guiWidth();
        int centerX = screenW / 2;
        int y = 4;

        // Player yaw: 0=south, 90=west, 180=north, 270=east (MC convention)
        float yaw = mc.player.getYRot() % 360;
        if (yaw < 0) yaw += 360;

        float pixelsPerDegree = (BAR_HALF_WIDTH * 2f) / VISIBLE_DEGREES;

        // Enable scissor to clip the compass bar
        g.enableScissor(centerX - BAR_HALF_WIDTH, y - 2, centerX + BAR_HALF_WIDTH, y + 12);

        // Draw cardinal labels
        for (int i = 0; i < LABELS.length; i++) {
            float offset = angleDiff(LABEL_YAWS[i], yaw) * pixelsPerDegree;
            int lx = centerX + (int) offset;

            if (lx < centerX - BAR_HALF_WIDTH - 20 || lx > centerX + BAR_HALF_WIDTH + 20) continue;

            // Fade alpha based on distance from center
            float dist = Math.abs(offset) / BAR_HALF_WIDTH;
            int alpha = (int) (255 * (1f - dist * dist * 0.7f));
            alpha = Math.max(40, Math.min(255, alpha));

            int color = (LABEL_COLORS[i] & 0x00FFFFFF) | (alpha << 24);

            String label = LABELS[i];
            int labelW = mc.font.width(label);
            g.drawString(mc.font, label, lx - labelW / 2, y, color, false);
        }

        // Draw degree tick marks every 15 degrees
        for (int deg = 0; deg < 360; deg += 15) {
            // Skip cardinal/intercardinal positions (they have labels)
            boolean isLabeled = false;
            for (float ly : LABEL_YAWS) {
                if (Math.abs(deg - ly) < 0.1f) { isLabeled = true; break; }
            }
            if (isLabeled) continue;

            float offset = angleDiff(deg, yaw) * pixelsPerDegree;
            int tx = centerX + (int) offset;
            if (tx < centerX - BAR_HALF_WIDTH || tx > centerX + BAR_HALF_WIDTH) continue;

            float dist = Math.abs(offset) / BAR_HALF_WIDTH;
            int alpha = (int) (180 * (1f - dist * dist * 0.8f));
            alpha = Math.max(20, Math.min(180, alpha));
            int tickColor = (alpha << 24) | 0x666677;

            // Small tick mark
            g.fill(tx, y + 2, tx + 1, y + 7, tickColor);
        }

        // Center marker (small triangle/line to show where you're looking)
        g.fill(centerX, y - 1, centerX + 1, y + 10, 0xAAFFFFFF);

        g.disableScissor();
    }

    /**
     * Returns the signed shortest angular difference from angle 'a' to the player yaw 'playerYaw'.
     * Result: negative = a is to the left, positive = a is to the right.
     */
    private static float angleDiff(float a, float playerYaw) {
        float diff = a - playerYaw;
        while (diff > 180) diff -= 360;
        while (diff < -180) diff += 360;
        return diff;
    }
}
