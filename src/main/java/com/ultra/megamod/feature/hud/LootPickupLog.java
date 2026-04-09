package com.ultra.megamod.feature.hud;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Bottom-right scrolling loot pickup notifications.
 * Stacks identical items within 2 seconds. Fades after 3 seconds.
 */
public class LootPickupLog {

    private static final List<LootEntry> ENTRIES = new ArrayList<>();
    private static final long DISPLAY_MS = 3000;
    private static final long STACK_WINDOW_MS = 2000;
    private static final int MAX_ENTRIES = 6;
    private static final int GOLD = 0xFFFFAA00;
    private static final int WHITE = 0xFFEEEEEE;

    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(Identifier.fromNamespaceAndPath("megamod", "loot_pickup_log"),
            LootPickupLog::render);
    }

    public static void addEntry(String itemName, int count) {
        long now = System.currentTimeMillis();
        // Try to stack with recent identical entry
        for (int i = ENTRIES.size() - 1; i >= 0; i--) {
            LootEntry e = ENTRIES.get(i);
            if (e.name.equals(itemName) && (now - e.addedMs) < STACK_WINDOW_MS) {
                e.count += count;
                e.addedMs = now; // Reset timer on stack
                return;
            }
        }
        // New entry
        ENTRIES.add(new LootEntry(itemName, count, now));
        // Trim old
        while (ENTRIES.size() > MAX_ENTRIES * 2) ENTRIES.remove(0);
    }

    private static void render(GuiGraphics g, DeltaTracker dt) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) return;

        long now = System.currentTimeMillis();
        // Remove expired
        Iterator<LootEntry> it = ENTRIES.iterator();
        while (it.hasNext()) {
            if (now - it.next().addedMs > DISPLAY_MS) it.remove();
        }
        if (ENTRIES.isEmpty()) return;

        int screenW = g.guiWidth();
        int screenH = g.guiHeight();

        int count = Math.min(ENTRIES.size(), MAX_ENTRIES);
        int y = screenH - 45 - (count - 1) * 12;

        for (int i = Math.max(0, ENTRIES.size() - MAX_ENTRIES); i < ENTRIES.size(); i++) {
            LootEntry e = ENTRIES.get(i);
            long elapsed = now - e.addedMs;

            // Fade in last 1 second
            float alpha = 1.0f;
            if (elapsed > 2000) {
                alpha = 1.0f - (float)(elapsed - 2000) / 1000.0f;
            }
            int alphaInt = Math.max(4, (int)(alpha * 255) & 0xFF);

            String text = "+" + e.count + " " + e.name;
            int textW = mc.font.width(text);
            int x = screenW - textW - 6;

            // Draw with alpha
            int goldAlpha = (alphaInt << 24) | (GOLD & 0x00FFFFFF);
            int whiteAlpha = (alphaInt << 24) | (WHITE & 0x00FFFFFF);

            // "+" in gold, item name in white
            String prefix = "+" + e.count + " ";
            g.drawString(mc.font, prefix, x, y, goldAlpha, false);
            g.drawString(mc.font, e.name, x + mc.font.width(prefix), y, whiteAlpha, false);

            y += 12;
        }
    }

    private static class LootEntry {
        final String name;
        int count;
        long addedMs;

        LootEntry(String name, int count, long addedMs) {
            this.name = name;
            this.count = count;
            this.addedMs = addedMs;
        }
    }
}
