package com.ultra.megamod.feature.hud;

import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;

/**
 * Transient centered HUD popup that appears when a weapon ability fires —
 * either from a right-click cast (MANUAL) or from a passive on-hit trigger (PASSIVE).
 *
 * <p>Client-side only. The server broadcasts an {@code AbilityTriggerPayload} which
 * sets {@link #currentMessage} and {@link #shownAtMs}. This layer fades the text
 * out over {@link #DURATION_MS} milliseconds.</p>
 */
public class AbilityTriggerHud {

    public static final long DURATION_MS = 1500L;

    public enum Kind {
        MANUAL,
        PASSIVE;

        public static Kind fromOrdinal(int o) {
            return o == 1 ? PASSIVE : MANUAL;
        }
    }

    public static volatile String currentMessage = "";
    public static volatile Kind currentKind = Kind.MANUAL;
    public static volatile long shownAtMs = 0;

    /** Called client-side when a trigger payload arrives. */
    public static void show(String label, Kind kind) {
        currentMessage = label;
        currentKind = kind;
        shownAtMs = System.currentTimeMillis();
    }

    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(Identifier.fromNamespaceAndPath("megamod", "ability_trigger"),
                AbilityTriggerHud::render);
    }

    private static void render(GuiGraphics g, DeltaTracker dt) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) return;

        String msg = currentMessage;
        if (msg == null || msg.isEmpty()) return;

        long elapsed = System.currentTimeMillis() - shownAtMs;
        if (elapsed > DURATION_MS) return;

        float t = elapsed / (float) DURATION_MS;
        int alpha;
        if (t < 0.2f) {
            alpha = (int) ((t / 0.2f) * 255);
        } else if (t > 0.6f) {
            alpha = (int) (((1.0f - t) / 0.4f) * 255);
        } else {
            alpha = 255;
        }
        alpha = Math.max(0, Math.min(255, alpha));

        Kind kind = currentKind;
        String prefix = kind == Kind.PASSIVE ? "\u2728 Passive " : "\u2694 Ability ";
        int baseColor = kind == Kind.PASSIVE ? 0x55FF55 : 0xFFD700;
        int textColor = (alpha << 24) | baseColor;
        int shadowColor = (alpha << 24);

        String full = prefix + msg;
        int w = mc.font.width(full);
        int screenW = g.guiWidth();
        int screenH = g.guiHeight();
        // Upper-center, above the hotbar crosshair area
        int x = (screenW - w) / 2;
        int y = screenH / 2 - 60;

        // Background pill
        int padX = 8;
        int padY = 4;
        int bgAlpha = (int) (alpha * 0.55f);
        int bgColor = (bgAlpha << 24) | 0x000000;
        g.fill(x - padX, y - padY, x + w + padX, y + 10 + padY, bgColor);
        g.fill(x - padX, y - padY, x + w + padX, y - padY + 1, (alpha << 24) | baseColor);
        g.fill(x - padX, y + 10 + padY - 1, x + w + padX, y + 10 + padY, (alpha << 24) | baseColor);

        g.drawString(mc.font, full, x + 1, y + 1, shadowColor, false);
        g.drawString(mc.font, full, x,     y,     textColor,   false);
    }
}
