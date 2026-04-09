package com.ultra.megamod.feature.dimensions.client;

import com.ultra.megamod.feature.dimensions.network.DimensionSyncPayload;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.Font;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.GuiLayerManager;

/**
 * Full-screen transition overlay shown when teleporting between dimensions.
 * Displays a themed title + subtitle with fade-in/hold/fade-out animation.
 */
public class DimensionTransitionOverlay {

    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(
            Identifier.fromNamespaceAndPath("megamod", "dimension_transition"),
            DimensionTransitionOverlay::render);
    }

    private static void render(GuiGraphics g, DeltaTracker dt) {
        String title = DimensionSyncPayload.transitionTitle;
        long startMs = DimensionSyncPayload.transitionStartMs;

        if (title == null || title.isEmpty() || startMs == 0) return;

        long elapsed = System.currentTimeMillis() - startMs;
        long duration = DimensionSyncPayload.TRANSITION_DURATION_MS;

        if (elapsed > duration) {
            DimensionSyncPayload.transitionTitle = "";
            DimensionSyncPayload.transitionStartMs = 0;
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        Font font = mc.font;
        int screenW = mc.getWindow().getGuiScaledWidth();
        int screenH = mc.getWindow().getGuiScaledHeight();

        float progress = (float) elapsed / duration;
        int bgColor = DimensionSyncPayload.transitionColor;

        // Animation phases:
        // 0.0-0.2: fade in (black to themed color)
        // 0.2-0.7: hold with text visible
        // 0.7-1.0: fade out
        float bgAlpha;
        float textAlpha;

        if (progress < 0.2f) {
            // Fade in
            float t = progress / 0.2f;
            bgAlpha = t;
            textAlpha = t;
        } else if (progress < 0.7f) {
            // Hold
            bgAlpha = 1.0f;
            textAlpha = 1.0f;
        } else {
            // Fade out
            float t = (progress - 0.7f) / 0.3f;
            bgAlpha = 1.0f - t;
            textAlpha = 1.0f - t;
        }

        // Draw fullscreen background with alpha
        int bgA = (int)(bgAlpha * 220) & 0xFF;
        int bgR = (bgColor >> 16) & 0xFF;
        int bgG = (bgColor >> 8) & 0xFF;
        int bgB = bgColor & 0xFF;
        int finalBg = (bgA << 24) | (bgR << 16) | (bgG << 8) | bgB;
        g.fill(0, 0, screenW, screenH, finalBg);

        // Draw decorative lines
        int lineAlpha = (int)(bgAlpha * 80) & 0xFF;
        int lineColor = (lineAlpha << 24) | 0xFFFFFF;
        int centerY = screenH / 2;
        int lineW = (int)(screenW * 0.4f);
        int lineX = (screenW - lineW) / 2;
        g.fill(lineX, centerY - 20, lineX + lineW, centerY - 19, lineColor);
        g.fill(lineX, centerY + 28, lineX + lineW, centerY + 29, lineColor);

        // Draw title (large, centered)
        int textA = (int)(textAlpha * 255) & 0xFF;
        if (textA > 10) {
            int titleColor = (textA << 24) | 0xFFFFFF;
            // Draw title at center without scaling
            g.drawCenteredString(font, title, screenW / 2, centerY - 12, titleColor);

            // Draw subtitle
            String subtitle = DimensionSyncPayload.transitionSubtitle;
            if (subtitle != null && !subtitle.isEmpty()) {
                int subColor = (textA << 24) | 0xBBBBBB;
                g.drawCenteredString(font, subtitle, screenW / 2, centerY + 14, subColor);
            }

            // Draw small decorative dots
            int dotColor = ((textA / 2) << 24) | 0xFFFFFF;
            for (int i = 0; i < 3; i++) {
                int dotX = screenW / 2 - 8 + i * 8;
                g.fill(dotX, centerY + 34, dotX + 2, centerY + 36, dotColor);
            }
        }
    }
}
