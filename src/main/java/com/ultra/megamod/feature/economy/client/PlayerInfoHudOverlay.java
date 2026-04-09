package com.ultra.megamod.feature.economy.client;

import com.ultra.megamod.feature.economy.network.PlayerInfoSyncPayload;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;

public class PlayerInfoHudOverlay {

    private static final int PANEL_BG = 0xAA101010;
    private static final int PANEL_BORDER = 0x88444444;
    private static final int GOLD_COLOR = 0xFFDAA520;
    private static final int BANK_COLOR = 0xFF88CCFF;
    private static final int LEVEL_COLOR = 0xFF55FF55;
    private static final int STAR_COLOR = 0xFFFFAA00;
    private static final int TIME_COLOR = 0xFFCCCCCC;
    private static final int DAY_COLOR = 0xFFAADDFF;
    private static final int WHITE = 0xFFE0E0E0;
    private static final int FPS_COLOR = 0xFFAAFFAA;
    private static final int COORD_COLOR = 0xFFFFCC66;
    private static final int PADDING = 4;
    private static final int LINE_H = 10;

    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(
                Identifier.fromNamespaceAndPath("megamod", "player_info_hud"),
                PlayerInfoHudOverlay::render);
    }

    private static void render(GuiGraphics g, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) return;

        int totalLevel = PlayerInfoSyncPayload.clientTotalLevel;
        int wallet = PlayerInfoSyncPayload.clientWallet;
        int bank = PlayerInfoSyncPayload.clientBank;
        String badgeTitle = PlayerInfoSyncPayload.clientBadgeTitle;
        String badgeColorCode = PlayerInfoSyncPayload.clientBadgeColorCode;
        int prestige = PlayerInfoSyncPayload.clientTotalPrestige;

        // Don't show if no data synced yet
        if (totalLevel == 0 && wallet == 0 && bank == 0 && badgeTitle.isEmpty()) return;

        Font font = mc.font;
        int x = 4, y = 4;

        // Build display strings
        String levelStr = "Lv " + totalLevel;
        String walletStr = "Wallet: " + formatCoins(wallet);
        String bankStr = "Bank: " + formatCoins(bank);

        String badgeStr = !badgeTitle.isEmpty() ? "[" + badgeTitle + "]" : "";
        String starStr = prestige > 0 ? "\u2605".repeat(Math.min(prestige, 25)) : "";

        // Day and time from world
        long dayTime = mc.level != null ? mc.level.getDayTime() : 0;
        int day = (int) (dayTime / 24000L) + 1;
        String dayStr = "Day " + day;
        String timeStr = formatWorldTime(dayTime);
        String dayTimeStr = dayStr + " | " + timeStr;

        // FPS and coordinates
        String fpsStr = mc.getFps() + " FPS";
        String biomeName = mc.level != null
                ? mc.level.getBiome(mc.player.blockPosition()).unwrapKey()
                        .map(key -> toTitleCase(key.identifier().getPath()))
                        .orElse("Unknown")
                : "Unknown";
        String coordStr = String.format("X: %.0f  Y: %.0f  Z: %.0f | %s",
                mc.player.getX(), mc.player.getY(), mc.player.getZ(), biomeName);

        // Measure panel width
        int line1W = font.width(levelStr);
        if (!badgeStr.isEmpty()) line1W += 4 + font.width(badgeStr);
        if (prestige > 0) line1W += 2 + font.width(starStr);
        int maxW = Math.max(line1W, Math.max(font.width(walletStr),
                  Math.max(font.width(bankStr), Math.max(font.width(dayTimeStr),
                  Math.max(font.width(fpsStr), font.width(coordStr))))));
        int panelW = maxW + PADDING * 2;
        int panelH = LINE_H * 6 + PADDING * 2;

        // Background
        g.fill(x, y, x + panelW, y + panelH, PANEL_BG);
        // Border
        g.fill(x, y, x + panelW, y + 1, PANEL_BORDER);
        g.fill(x, y + panelH - 1, x + panelW, y + panelH, PANEL_BORDER);
        g.fill(x, y, x + 1, y + panelH, PANEL_BORDER);
        g.fill(x + panelW - 1, y, x + panelW, y + panelH, PANEL_BORDER);

        int tx = x + PADDING;
        int ty = y + PADDING;

        // Line 1: Level + Badge + Prestige stars
        g.drawString(font, levelStr, tx, ty, LEVEL_COLOR, false);
        int badgeX = tx + font.width(levelStr) + 4;
        if (!badgeStr.isEmpty()) {
            g.drawString(font, badgeColorCode + badgeStr, badgeX, ty, WHITE, false);
            badgeX += font.width(badgeStr) + 2;
        }
        if (prestige > 0) {
            g.drawString(font, starStr, badgeX, ty, STAR_COLOR, false);
        }
        ty += LINE_H;

        // Line 2: Wallet
        g.drawString(font, walletStr, tx, ty, GOLD_COLOR, false);
        ty += LINE_H;

        // Line 3: Bank
        g.drawString(font, bankStr, tx, ty, BANK_COLOR, false);
        ty += LINE_H;

        // Line 4: Day and Time
        g.drawString(font, dayStr, tx, ty, DAY_COLOR, false);
        int pipeX = tx + font.width(dayStr);
        g.drawString(font, " | ", pipeX, ty, 0xFF666666, false);
        g.drawString(font, timeStr, pipeX + font.width(" | "), ty, TIME_COLOR, false);
        ty += LINE_H;

        // Line 5: FPS
        g.drawString(font, fpsStr, tx, ty, FPS_COLOR, false);
        ty += LINE_H;

        // Line 6: Coordinates + Facing
        g.drawString(font, coordStr, tx, ty, COORD_COLOR, false);
    }

    private static String formatCoins(int amount) {
        return String.format("%,d", amount);
    }

    private static String toTitleCase(String snakeCase) {
        StringBuilder sb = new StringBuilder();
        for (String word : snakeCase.split("_")) {
            if (!word.isEmpty()) {
                if (!sb.isEmpty()) sb.append(' ');
                sb.append(Character.toUpperCase(word.charAt(0)));
                if (word.length() > 1) sb.append(word.substring(1));
            }
        }
        return sb.toString();
    }

    private static String formatWorldTime(long dayTime) {
        // Minecraft time: 0 = 6:00 AM, 6000 = noon, 12000 = 6:00 PM, 18000 = midnight
        int ticksInDay = (int) (dayTime % 24000L);
        int hours = (ticksInDay / 1000 + 6) % 24;
        int minutes = (ticksInDay % 1000) * 60 / 1000;
        String period = hours >= 12 ? "PM" : "AM";
        int displayHour = hours % 12;
        if (displayHour == 0) displayHour = 12;
        return String.format("%d:%02d %s", displayHour, minutes, period);
    }
}
