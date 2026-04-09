package com.ultra.megamod.feature.hud;

import com.ultra.megamod.feature.hud.network.ComboPayload;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;

/**
 * Displays kill combo counter on the right side of the screen.
 * Large "xN" text with scale animation on increment and timer bar.
 */
public class KillComboDisplay {

    private static int lastDisplayedCombo = 0;
    private static long lastBumpMs = 0;

    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(Identifier.fromNamespaceAndPath("megamod", "kill_combo"),
            KillComboDisplay::render);
    }

    private static void render(GuiGraphics g, DeltaTracker dt) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) return;

        int combo = ComboPayload.clientCombo;
        if (combo <= 1) {
            lastDisplayedCombo = 0;
            return;
        }

        // Detect combo increment for bump animation
        if (combo != lastDisplayedCombo) {
            lastDisplayedCombo = combo;
            lastBumpMs = System.currentTimeMillis();
        }

        int screenW = g.guiWidth();
        int screenH = g.guiHeight();

        // Position: center-right
        int x = screenW - 70;
        int y = screenH / 2 - 20;

        // Combo text
        String comboText = "x" + combo;

        // Scale bump animation (first 200ms)
        long elapsed = System.currentTimeMillis() - lastBumpMs;
        float scale = 2.0f;
        if (elapsed < 200) {
            float t = (float) elapsed / 200.0f;
            scale = 2.0f + 0.8f * (1.0f - t * t); // Ease-out bump
        }

        // Color: escalate from white to gold to red
        int color;
        if (combo >= 20) color = 0xFFFF4444;      // Red for insane combos
        else if (combo >= 10) color = 0xFFFFAA00;  // Gold
        else if (combo >= 5) color = 0xFFFFFF00;   // Yellow
        else color = 0xFFFFFFFF;                    // White

        int textW = mc.font.width(comboText);

        // Draw at computed position without scaling (API changed in 1.21.11)
        int drawX = x - textW / 2;
        // Shadow
        g.drawString(mc.font, comboText, drawX + 1, y + 1, 0x44000000, false);
        // Main
        g.drawString(mc.font, comboText, drawX, y, color, false);

        // Timer bar below combo text
        int barW = 40;
        int barH = 3;
        int barX = x - barW / 2;
        int barY = y + (int)(scale * 10) + 4;

        // Calculate timer progress
        long timeSinceUpdate = System.currentTimeMillis() - ComboPayload.lastUpdateMs;
        float timerProgress = Math.max(0, 1.0f - (float) timeSinceUpdate / (ComboPayload.clientTimer * 50.0f));

        // Bar background
        g.fill(barX, barY, barX + barW, barY + barH, 0x44FFFFFF);
        // Bar fill
        int fillW = (int)(barW * timerProgress);
        if (fillW > 0) {
            g.fill(barX, barY, barX + fillW, barY + barH, color);
        }

        // "COMBO" label
        String label = "COMBO";
        int labelW = mc.font.width(label);
        g.drawString(mc.font, label, x - labelW / 2, barY + 5, 0x88FFFFFF, false);
    }
}
