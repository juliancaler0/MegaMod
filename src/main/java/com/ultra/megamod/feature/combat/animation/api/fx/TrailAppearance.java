package com.ultra.megamod.feature.combat.animation.api.fx;

import org.jetbrains.annotations.Nullable;

/**
 * Defines the visual appearance of weapon trail particles (primary + secondary layers).
 * Ported 1:1 from BetterCombat (net.bettercombat.api.fx.TrailAppearance).
 */
public class TrailAppearance {
    public record Part(long color_rgba, boolean glows) {
        public static final Part DEFAULT_PRIMARY = new Part(0xFFFFFFFFL, false);
        public static final Part DEFAULT_SECONDARY = new Part(0x99999999L, false);
    }

    public Part primary = Part.DEFAULT_PRIMARY;
    public Part secondary = Part.DEFAULT_SECONDARY;
    public static final TrailAppearance DEFAULT = new TrailAppearance();

    public TrailAppearance() {}

    public TrailAppearance(Part primary, @Nullable Part secondary) {
        this.primary = primary;
        this.secondary = secondary;
    }
}
