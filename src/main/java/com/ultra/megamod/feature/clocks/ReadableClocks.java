/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.DeltaTracker
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.resources.Identifier
 *  net.minecraft.world.item.Items
 *  net.neoforged.neoforge.client.event.RegisterGuiLayersEvent
 */
package com.ultra.megamod.feature.clocks;

import com.ultra.megamod.feature.ui.UIHelper;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;

public class ReadableClocks {
    private static final int HUD_BG = -1273360888;
    private static final int HUD_BORDER = 1355327564;
    private static final int HUD_TEXT = -991040;
    private static final int HUD_TEXT_DIM = -5728136;
    private static final int HUD_ACCENT = -2448096;

    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(Identifier.fromNamespaceAndPath((String)"megamod", (String)"readable_clocks"), ReadableClocks::renderClockDisplay);
    }

    private static void renderClockDisplay(GuiGraphics graphics, DeltaTracker partialTick) {
        boolean holdingClock;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return;
        }
        boolean bl = holdingClock = mc.player.getMainHandItem().is(Items.CLOCK) || mc.player.getOffhandItem().is(Items.CLOCK);
        if (!holdingClock) {
            return;
        }
        long dayTime = mc.level.getDayTime();
        long currentDay = dayTime / 24000L + 1L;
        long ticksInDay = dayTime % 24000L;
        String dayPart = "Day " + currentDay;
        String separator = " \u2014 ";
        String timePart = ReadableClocks.formatTimePart(ticksInDay);
        String ampmPart = " " + ReadableClocks.formatAmPm(ticksInDay);
        int dayWidth = mc.font.width(dayPart);
        int sepWidth = mc.font.width(separator);
        int timeWidth = mc.font.width(timePart);
        int ampmWidth = mc.font.width(ampmPart);
        int totalWidth = dayWidth + sepWidth + timeWidth + ampmWidth;
        int screenWidth = graphics.guiWidth();
        int x = (screenWidth - totalWidth) / 2;
        int y = 10;
        int panelPadX = 6;
        int panelPadY = 4;
        ReadableClocks.drawHudPanel(graphics, x - panelPadX, y - panelPadY, totalWidth + panelPadX * 2, 9 + panelPadY * 2);
        graphics.drawString(mc.font, dayPart, x, y, -991040);
        graphics.drawString(mc.font, separator, x += dayWidth, y, -5728136);
        graphics.drawString(mc.font, timePart, x += sepWidth, y, -2448096);
        graphics.drawString(mc.font, ampmPart, x += timeWidth, y, -5728136);
    }

    private static void drawHudPanel(GuiGraphics g, int x, int y, int w, int h) {
        UIHelper.drawHudPanel(g, x, y, w, h);
    }

    private static String formatTime(long ticksInDay) {
        long adjustedTicks = (ticksInDay + 6000L) % 24000L;
        int totalMinutes = (int)(adjustedTicks * 60L / 1000L);
        int hours24 = totalMinutes / 60;
        int minutes = totalMinutes % 60;
        String period = hours24 >= 12 ? "PM" : "AM";
        int hours12 = hours24 % 12;
        if (hours12 == 0) {
            hours12 = 12;
        }
        return String.format("%d:%02d %s", hours12, minutes, period);
    }

    private static String formatTimePart(long ticksInDay) {
        long adjustedTicks = (ticksInDay + 6000L) % 24000L;
        int totalMinutes = (int)(adjustedTicks * 60L / 1000L);
        int hours24 = totalMinutes / 60;
        int minutes = totalMinutes % 60;
        int hours12 = hours24 % 12;
        if (hours12 == 0) {
            hours12 = 12;
        }
        return String.format("%d:%02d", hours12, minutes);
    }

    private static String formatAmPm(long ticksInDay) {
        long adjustedTicks = (ticksInDay + 6000L) % 24000L;
        int totalMinutes = (int)(adjustedTicks * 60L / 1000L);
        int hours24 = totalMinutes / 60;
        return hours24 >= 12 ? "PM" : "AM";
    }
}

