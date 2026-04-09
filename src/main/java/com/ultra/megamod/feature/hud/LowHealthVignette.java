package com.ultra.megamod.feature.hud;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;

/**
 * Pulsing red vignette overlay when player health drops below 25%.
 * Intensity scales with how close to death the player is.
 */
public class LowHealthVignette {

    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(Identifier.fromNamespaceAndPath("megamod", "low_health_vignette"),
            LowHealthVignette::render);
    }

    private static void render(GuiGraphics g, DeltaTracker dt) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) return;

        float hp = mc.player.getHealth();
        float maxHp = mc.player.getMaxHealth();
        if (maxHp <= 0 || hp / maxHp >= 0.25f) return;

        // Intensity: 0 at 25%, 1 at 0% HP
        float intensity = 1.0f - (hp / maxHp) / 0.25f;
        // Pulse: oscillating alpha
        float pulse = 0.15f + 0.12f * (float) Math.sin(System.currentTimeMillis() / 250.0);
        float baseAlpha = pulse * intensity;

        int w = g.guiWidth();
        int h = g.guiHeight();
        int strips = 35;

        // Draw gradient strips along all 4 edges
        for (int i = 0; i < strips; i++) {
            float fade = 1.0f - (float) i / strips;
            fade = fade * fade; // Quadratic falloff for natural gradient
            int alpha = (int) (baseAlpha * fade * 255) & 0xFF;
            if (alpha < 2) continue;
            int color = (alpha << 24) | 0xCC0000;

            // Left edge
            g.fill(i, 0, i + 1, h, color);
            // Right edge
            g.fill(w - i - 1, 0, w - i, h, color);
            // Top edge
            g.fill(0, i, w, i + 1, color);
            // Bottom edge
            g.fill(0, h - i - 1, w, h - i, color);
        }
    }
}
