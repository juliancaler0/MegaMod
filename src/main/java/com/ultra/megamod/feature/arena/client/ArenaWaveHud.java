package com.ultra.megamod.feature.arena.client;

import com.ultra.megamod.feature.arena.network.ArenaCheckpointPayload;
import com.ultra.megamod.feature.arena.network.ArenaHudSyncPayload;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;

/**
 * HUD overlay showing the current arena wave number.
 * Displays at top-center of screen when player is in an arena.
 */
public class ArenaWaveHud {

    // Read from ArenaHudSyncPayload static fields (server-safe payload storage)

    private static final int BG = 0xCC0A0A14;
    private static final int BORDER = 0xFFD4AF37;
    private static final int WAVE_COLOR = 0xFFFFDD44;
    private static final int MOBS_COLOR = 0xFFCC4444;
    private static final int DIM = 0xFF888899;

    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(Identifier.fromNamespaceAndPath("megamod", "arena_wave_hud"),
                ArenaWaveHud::render);
    }

    private static void render(GuiGraphics g, DeltaTracker dt) {
        boolean inArena = ArenaHudSyncPayload.clientInArena;
        int currentWave = ArenaHudSyncPayload.clientWave;
        int mobsAlive = ArenaHudSyncPayload.clientMobsAlive;
        if (!inArena) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) return;

        Font font = mc.font;
        int screenW = g.guiWidth();

        // Build text
        String waveStr = "WAVE " + currentWave;
        String mobStr = mobsAlive > 0 ? mobsAlive + " enemies remaining" : "Wave cleared!";
        int mobColor = mobsAlive > 0 ? MOBS_COLOR : 0xFF44CC44;

        int waveW = font.width(waveStr);
        int mobW = font.width(mobStr);
        int maxW = Math.max(waveW, mobW);
        int panelW = maxW + 16;
        int panelH = 26;
        int panelX = (screenW - panelW) / 2;
        int panelY = 6;

        // Background
        g.fill(panelX, panelY, panelX + panelW, panelY + panelH, BG);
        // Gold border
        g.fill(panelX, panelY, panelX + panelW, panelY + 1, BORDER);
        g.fill(panelX, panelY + panelH - 1, panelX + panelW, panelY + panelH, BORDER);
        g.fill(panelX, panelY, panelX + 1, panelY + panelH, BORDER);
        g.fill(panelX + panelW - 1, panelY, panelX + panelW, panelY + panelH, BORDER);

        // Wave number (centered, line 1)
        g.drawString(font, waveStr, panelX + (panelW - waveW) / 2, panelY + 4, WAVE_COLOR, false);
        // Mobs remaining (centered, line 2)
        g.drawString(font, mobStr, panelX + (panelW - mobW) / 2, panelY + 15, mobColor, false);
    }
}
