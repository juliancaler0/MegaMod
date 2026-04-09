package com.ultra.megamod.feature.hud.combos;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.Font;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;

/**
 * Client-side HUD overlay that displays combat combo names
 * with a scale-up animation and fade-out.
 */
public class CombatComboDisplay {

    private static final long DISPLAY_DURATION_MS = 2000; // 2 seconds

    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(
            Identifier.fromNamespaceAndPath("megamod", "combat_combo"),
            CombatComboDisplay::render);
    }

    private static void render(GuiGraphics g, DeltaTracker dt) {
        String comboName = CombatComboPayload.lastComboName;
        long comboTime = CombatComboPayload.lastComboTime;
        int comboColor = CombatComboPayload.lastComboColor;

        if (comboName.isEmpty() || comboTime == 0) return;

        long elapsed = System.currentTimeMillis() - comboTime;
        if (elapsed > DISPLAY_DURATION_MS) {
            CombatComboPayload.lastComboName = "";
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        Font font = mc.font;
        int screenW = mc.getWindow().getGuiScaledWidth();
        int screenH = mc.getWindow().getGuiScaledHeight();

        // Fade and scale animation
        float progress = (float) elapsed / DISPLAY_DURATION_MS;
        float alpha;
        float scale;
        if (progress < 0.15f) {
            // Scale up phase
            float t = progress / 0.15f;
            scale = 1.5f + (2.5f - 1.5f) * t; // 1.5 -> 2.5
            alpha = t;
        } else if (progress < 0.4f) {
            // Hold phase
            scale = 2.5f;
            alpha = 1.0f;
        } else {
            // Fade out phase
            float t = (progress - 0.4f) / 0.6f;
            scale = 2.5f - t * 0.5f; // 2.5 -> 2.0
            alpha = 1.0f - t;
        }

        if (alpha <= 0) return;

        int textWidth = font.width(comboName);
        int x = screenW / 2;
        int y = screenH / 2 - 40;

        // Apply alpha to color
        int a = (int)(alpha * 255) & 0xFF;
        int finalColor = (a << 24) | (comboColor & 0x00FFFFFF);

        // Draw at computed position without scaling (API changed in 1.21.11)
        g.drawCenteredString(font, comboName, x, y, finalColor);
    }
}
