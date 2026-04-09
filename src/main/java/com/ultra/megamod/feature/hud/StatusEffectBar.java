package com.ultra.megamod.feature.hud;

import com.ultra.megamod.feature.ui.UIHelper;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;

import java.util.Collection;

/**
 * Compact status effect bar. Each effect shows as a two-line entry:
 *   Line 1: colored dot + effect name
 *   Line 2: duration right-aligned
 * Laid out in a vertical column on the top-left of the screen.
 */
public class StatusEffectBar {

    private static final int ENTRY_WIDTH = 80;
    private static final int ENTRY_HEIGHT = 14;
    private static final int MAX_EFFECTS = 10;
    private static final int GAP = 1;

    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(Identifier.fromNamespaceAndPath("megamod", "status_effects"),
            StatusEffectBar::render);
    }

    private static void render(GuiGraphics g, DeltaTracker dt) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) return;

        Collection<MobEffectInstance> effects = mc.player.getActiveEffects();
        if (effects.isEmpty()) return;

        int count = Math.min(effects.size(), MAX_EFFECTS);

        // Pre-measure widest entry so the panel can expand to fit long names
        int entryWidth = ENTRY_WIDTH; // minimum width
        int idx = 0;
        for (MobEffectInstance effect : effects) {
            if (idx >= MAX_EFFECTS) break;
            String name = getDisplayName(effect);
            String durStr = formatDuration(effect.getDuration());
            // dot(4) + gap(7) + name + gap(6) + duration
            int needed = 7 + mc.font.width(name) + 6 + mc.font.width(durStr);
            if (needed > entryWidth) entryWidth = needed;
            idx++;
        }

        // Position: top-left corner, below the player info HUD
        int panelX = 4;
        int panelY = 76;
        int panelH = count * (ENTRY_HEIGHT + GAP) - GAP + 6;

        // Semi-transparent background (width adapts to content)
        g.fill(panelX, panelY, panelX + entryWidth + 8, panelY + panelH, 0x88111111);

        int x = panelX + 4;
        int y = panelY + 3;
        int drawn = 0;

        for (MobEffectInstance effect : effects) {
            if (drawn >= MAX_EFFECTS) break;

            boolean beneficial = effect.getEffect().value().isBeneficial();
            int dotColor = beneficial ? 0xFF44CC44 : 0xFFCC4444;

            // Colored dot
            g.fill(x, y + 2, x + 4, y + 6, dotColor);

            // Effect name
            String name = getDisplayName(effect);
            g.drawString(mc.font, name, x + 7, y, 0xFFDDDDDD, false);

            // Duration — right-aligned, same line
            int dur = effect.getDuration();
            String durStr = formatDuration(dur);
            int durW = mc.font.width(durStr);
            int durColor = dur < 200 ? 0xFFFF6666 : 0xFF999999; // flash red under 10s
            g.drawString(mc.font, durStr, x + entryWidth - durW, y, durColor, false);

            y += ENTRY_HEIGHT + GAP;
            drawn++;
        }
    }

    private static String formatDuration(int dur) {
        if (dur > 32000 * 20) {
            return "\u221E"; // infinity symbol
        }
        int totalSec = dur / 20;
        int min = totalSec / 60;
        int sec = totalSec % 60;
        return min > 0 ? min + ":" + String.format("%02d", sec) : sec + "s";
    }

    private static String getDisplayName(MobEffectInstance effect) {
        String id = effect.getEffect().value().getDescriptionId();
        int lastDot = id.lastIndexOf('.');
        String name = lastDot >= 0 ? id.substring(lastDot + 1) : id;

        // Convert snake_case to Title Case
        StringBuilder sb = new StringBuilder();
        for (String word : name.split("_")) {
            if (!word.isEmpty()) {
                if (!sb.isEmpty()) sb.append(' ');
                sb.append(Character.toUpperCase(word.charAt(0)));
                if (word.length() > 1) sb.append(word.substring(1));
            }
        }

        // Amplifier suffix
        int amp = effect.getAmplifier();
        if (amp > 0) {
            sb.append(' ').append(toRoman(amp + 1));
        }

        return sb.toString();
    }

    private static String toRoman(int n) {
        return switch (n) {
            case 2 -> "II"; case 3 -> "III"; case 4 -> "IV"; case 5 -> "V";
            case 6 -> "VI"; case 7 -> "VII"; case 8 -> "VIII"; case 9 -> "IX"; case 10 -> "X";
            default -> String.valueOf(n);
        };
    }
}
