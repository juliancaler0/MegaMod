package com.ultra.megamod.feature.combat.spell;

import com.ultra.megamod.feature.ui.UIHelper;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;

import java.util.Map;
import java.util.UUID;

/**
 * HUD overlay that shows a cast bar when the player is casting a charged/channeled spell.
 * Also renders small cast indicators above nearby players who are currently casting.
 * Registered in MegaModClient alongside other HUD layers.
 */
public class SpellCastOverlay {

    // Client-side state synced from server (local player)
    public static volatile String castingSpellId = null;
    public static volatile float castProgress = 0f; // 0.0 to 1.0
    public static volatile String castingSpellName = "";
    public static volatile int castingSchoolColor = 0xFFFFFFFF;

    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(Identifier.fromNamespaceAndPath("megamod", "spell_cast_bar"),
            SpellCastOverlay::render);
    }

    private static void render(GuiGraphics g, DeltaTracker dt) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) return;

        // Render local player's cast bar
        renderLocalCastBar(g, mc);

        // Render nearby players' cast indicators
        renderNearbyCastIndicators(g, mc);
    }

    private static void renderLocalCastBar(GuiGraphics g, Minecraft mc) {
        if (castingSpellId == null || castProgress <= 0) return;

        int screenW = g.guiWidth();
        int screenH = g.guiHeight();

        // Cast bar dimensions — centered horizontally, above hotbar
        int barW = 120;
        int barH = 10;
        int barX = (screenW - barW) / 2;
        int barY = screenH - 60; // above hotbar

        // Background
        g.fill(barX - 1, barY - 1, barX + barW + 1, barY + barH + 1, 0xCC000000);
        g.fill(barX, barY, barX + barW, barY + barH, 0xFF1A1A28);

        // Fill based on progress
        int fillW = (int)(barW * Math.min(castProgress, 1.0f));
        int fillColor = castingSchoolColor;
        g.fill(barX, barY, barX + fillW, barY + barH, fillColor);

        // Bright edge on fill
        if (fillW > 0) {
            g.fill(barX + fillW - 1, barY, barX + fillW, barY + barH,
                UIHelper.brightenColor(fillColor, 60));
        }

        // Spell name above bar
        if (!castingSpellName.isEmpty()) {
            int nameW = mc.font.width(castingSpellName);
            g.drawString(mc.font, castingSpellName, (screenW - nameW) / 2, barY - 12,
                castingSchoolColor, true);
        }

        // Progress percentage
        String pctText = (int)(castProgress * 100) + "%";
        g.drawCenteredString(mc.font, pctText, barX + barW / 2, barY + 1, 0xFFFFFFFF);
    }

    /**
     * Renders a small school-colored cast indicator next to each nearby player's name
     * in the top-left corner of the screen (compact party-style list).
     */
    private static void renderNearbyCastIndicators(GuiGraphics g, Minecraft mc) {
        Map<UUID, NearbyPlayerCastTracker.CastState> casters = NearbyPlayerCastTracker.getActiveCasters();
        if (casters.isEmpty()) return;

        UUID localId = mc.player.getUUID();
        int y = 4;

        for (Map.Entry<UUID, NearbyPlayerCastTracker.CastState> entry : casters.entrySet()) {
            UUID casterId = entry.getKey();
            if (casterId.equals(localId)) continue; // Skip self (already shown as main cast bar)

            NearbyPlayerCastTracker.CastState state = entry.getValue();

            // Find player entity for display name
            Player casterEntity = mc.level != null ? mc.level.getPlayerByUUID(casterId) : null;
            if (casterEntity == null) continue;

            // Only show for players within 32 blocks
            if (casterEntity.distanceTo(mc.player) > 32.0) continue;

            String playerName = casterEntity.getGameProfile().name();
            String label = playerName + " \u27A4 " + state.spellName();
            int color = state.schoolColor();

            // Small progress bar
            int barX = 4;
            int barW = 80;
            int barH = 4;

            // Draw label
            g.drawString(mc.font, label, barX, y, color, true);
            y += 10;

            // Draw mini cast bar
            g.fill(barX, y, barX + barW, y + barH, 0xAA000000);
            int fillW = (int)(barW * Math.min(state.progress(), 1.0f));
            if (fillW > 0) {
                g.fill(barX, y, barX + fillW, y + barH, color);
            }
            y += barH + 4;
        }
    }

    /**
     * Called from client tick to handle cast bar completion animation
     * and to clean up stale nearby player cast states.
     * Actual state is synced from the server via {@link SpellCastSyncPayload}.
     */
    public static void updateFromClientTick() {
        // Clean up stale nearby casters (no update for 2+ seconds)
        NearbyPlayerCastTracker.tick();
    }
}
