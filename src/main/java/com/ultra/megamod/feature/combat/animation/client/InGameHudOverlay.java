package com.ultra.megamod.feature.combat.animation.client;

import com.ultra.megamod.feature.combat.animation.AttackAnimationPayload;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
// LayeredDraw removed in 1.21.11 — using RegisterGuiLayersEvent directly
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.minecraft.resources.Identifier;

/**
 * Combo counter HUD overlay.
 * Ported from BetterCombat (net.bettercombat.mixin.client.InGameHudInject).
 */
public class InGameHudOverlay {

    private static final Identifier LAYER_ID = Identifier.fromNamespaceAndPath("megamod", "bc_combat_combo_hud");

    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(LAYER_ID, InGameHudOverlay::render);
    }

    private static void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        if (mc.options.hideGui) return;

        // Check for active attack animation
        var animData = AttackAnimationPayload.CLIENT_ANIMATIONS.get(mc.player.getId());
        if (animData == null || animData.isExpired()) return;

        int combo = animData.comboIndex() + 1;
        if (combo <= 1) return;

        // Render combo counter
        String text = "Combo x" + combo;
        int width = mc.font.width(text);
        int screenWidth = graphics.guiWidth();
        int x = (screenWidth - width) / 2;
        int y = graphics.guiHeight() / 2 - 30;

        // Fade based on time since last hit
        float alpha = 1.0f;
        long elapsed = System.currentTimeMillis() - animData.receivedTime();
        if (elapsed > 600) {
            alpha = Math.max(0, 1.0f - (elapsed - 600) / 400f);
        }

        if (alpha > 0.05f) {
            int color = ((int)(alpha * 255) << 24) | 0xFFCC00;
            graphics.drawString(mc.font, Component.literal(text), x, y, color, true);
        }
    }
}
