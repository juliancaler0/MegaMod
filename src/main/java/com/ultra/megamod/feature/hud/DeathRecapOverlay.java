package com.ultra.megamod.feature.hud;

import com.ultra.megamod.feature.hud.network.DeathRecapPayload;
import com.ultra.megamod.feature.ui.UIHelper;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Overlay shown on the death screen with damage breakdown.
 * Shows who killed you, damage sources, and total damage taken in last 5 seconds.
 */
public class DeathRecapOverlay {

    private static DeathRecapPayload recap = null;

    public static void setRecap(DeathRecapPayload payload) {
        recap = payload;
    }

    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(Identifier.fromNamespaceAndPath("megamod", "death_recap"),
            DeathRecapOverlay::render);
    }

    private static void render(GuiGraphics g, DeltaTracker dt) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        // Only render on death screen
        if (!(mc.screen instanceof DeathScreen)) {
            // Clear recap when leaving death screen (respawn)
            if (recap != null && mc.player.isAlive()) recap = null;
            return;
        }

        if (recap == null) return;

        int screenW = g.guiWidth();
        int screenH = g.guiHeight();

        // Aggregate damage by source
        Map<String, Float> bySource = new HashMap<>();
        float totalDamage = 0;
        for (DeathRecapPayload.DamageEntry e : recap.entries()) {
            bySource.merge(e.sourceName(), e.amount(), Float::sum);
            totalDamage += e.amount();
        }

        // Panel dimensions
        int panelW = 160;
        int lineCount = bySource.size();
        int panelH = 36 + lineCount * 14 + 14; // header + entries + total
        int panelX = 10;
        int panelY = screenH / 2 - panelH / 2;

        UIHelper.drawHudPanel(g, panelX, panelY, panelW, panelH);

        int tx = panelX + 6;
        int ty = panelY + 5;

        // Title
        g.drawString(mc.font, "DEATH RECAP", tx, ty, 0xFFFF4444, false);
        ty += 12;

        // Killer
        String killerText = "Killed by: " + recap.killerName();
        if (killerText.length() > 22) killerText = killerText.substring(0, 20) + "..";
        g.drawString(mc.font, killerText, tx, ty, 0xFFFFAA00, false);
        ty += 14;

        // Damage bars sorted by amount
        float maxDmg = bySource.values().stream().max(Float::compare).orElse(1f);
        int barMaxW = panelW - 12;

        List<Map.Entry<String, Float>> sorted = bySource.entrySet().stream()
            .sorted((a, b) -> Float.compare(b.getValue(), a.getValue()))
            .toList();

        for (Map.Entry<String, Float> entry : sorted) {
            String source = entry.getKey();
            float dmg = entry.getValue();

            // Color by damage type
            int barColor = getDamageColor(source);

            // Bar
            int barW = Math.max(2, (int)(barMaxW * (dmg / maxDmg)));
            g.fill(tx, ty + 1, tx + barW, ty + 8, barColor);

            // Label
            String label = source;
            if (label.length() > 10) label = label.substring(0, 8) + "..";
            g.drawString(mc.font, label, tx + 2, ty + 1, 0xFFFFFFFF, false);

            // Amount
            String dmgStr = String.format("%.1f", dmg);
            int dmgW = mc.font.width(dmgStr);
            g.drawString(mc.font, dmgStr, tx + barMaxW - dmgW, ty + 1, 0xFFCCCCCC, false);

            ty += 14;
        }

        // Total
        g.drawString(mc.font, "Total: " + String.format("%.1f", totalDamage), tx, ty, 0xFFFF6666, false);
    }

    private static int getDamageColor(String source) {
        String lower = source.toLowerCase();
        if (lower.contains("fire") || lower.contains("lava")) return 0xAAFF6622;
        if (lower.contains("fall")) return 0xAAFFCC44;
        if (lower.contains("drown") || lower.contains("water")) return 0xAA4488FF;
        if (lower.contains("magic") || lower.contains("wither")) return 0xAA8844AA;
        if (lower.contains("explosion") || lower.contains("creeper")) return 0xAAFF8800;
        return 0xAACC4444; // Default red for mob/player damage
    }
}
