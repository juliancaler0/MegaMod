/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.DeltaTracker
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.resources.Identifier
 *  net.neoforged.neoforge.client.event.RegisterGuiLayersEvent
 */
package com.ultra.megamod.feature.skills.client;

import java.util.Objects;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;

public class SkillHudOverlay {
    private static String lastTreeName = "";
    private static int lastXpAmount = 0;
    private static long showTimestamp = 0L;
    private static final long DISPLAY_DURATION_MS = 3000L;
    private static final int TEXT_COLOR = -2448096;
    private static final int TEXT_SHADOW = -15069688;
    private static final int PANEL_PADDING = 4;

    public static void showXpGain(String treeName, int amount) {
        if (amount <= 0) {
            return;
        }
        lastTreeName = treeName;
        lastXpAmount = amount;
        showTimestamp = System.currentTimeMillis();
    }

    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(Identifier.fromNamespaceAndPath((String)"megamod", (String)"skill_xp_hud"), SkillHudOverlay::renderSkillXpHud);
    }

    private static void renderSkillXpHud(GuiGraphics graphics, DeltaTracker deltaTracker) {
        int alphaInt;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) {
            return;
        }
        long elapsed = System.currentTimeMillis() - showTimestamp;
        if (elapsed > 3000L || lastXpAmount <= 0) {
            return;
        }
        Font font = mc.font;
        String text = "+" + lastXpAmount + " " + lastTreeName + " XP";
        float alpha = 1.0f;
        long fadeStart = 2000L;
        if (elapsed > fadeStart) {
            alpha = 1.0f - (float)(elapsed - fadeStart) / 1000.0f;
        }
        if ((alphaInt = (int)(alpha * 255.0f) & 0xFF) < 8) {
            return;
        }
        int textColor = alphaInt << 24 | 0xDAA520;
        int shadowColor = alphaInt << 24 | 0x1A0E08;
        int panelAlpha = (int)(alpha * 180.0f) & 0xFF;
        int panelColor = panelAlpha << 24 | 0x1A0E08;
        int screenWidth = graphics.guiWidth();
        int textWidth = font.width(text);
        int panelW = textWidth + 8;
        Objects.requireNonNull(font);
        int panelH = 9 + 8;
        int panelX = (screenWidth - panelW) / 2;
        int panelY = 10;
        if (elapsed < 200L) {
            int offset = (int)((1.0 - (double)elapsed / 200.0) * 10.0);
            panelY -= offset;
        }
        int borderAlpha = (int)(alpha * 200.0f) & 0xFF;
        int borderColor = borderAlpha << 24 | 0xC8A84C;
        graphics.fill(panelX - 1, panelY - 1, panelX + panelW + 1, panelY + panelH + 1, borderColor);
        graphics.fill(panelX, panelY, panelX + panelW, panelY + panelH, panelColor);
        int textX = panelX + 4;
        int textY = panelY + 4;
        graphics.drawString(font, text, textX + 1, textY + 1, shadowColor, false);
        graphics.drawString(font, text, textX, textY, textColor, false);
    }
}

