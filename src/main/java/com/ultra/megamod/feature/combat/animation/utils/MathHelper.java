package com.ultra.megamod.feature.combat.animation.utils;

/**
 * Math utilities for BetterCombat.
 * Ported from BetterCombat (net.bettercombat.utils.MathHelper).
 */
public class MathHelper {
    public static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public static double easeOutCubic(double x) {
        return 1 - Math.pow(1 - x, 3);
    }
}
