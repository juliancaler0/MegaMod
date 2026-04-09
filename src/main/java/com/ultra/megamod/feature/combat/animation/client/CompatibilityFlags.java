package com.ultra.megamod.feature.combat.animation.client;

/**
 * Feature flags for compatibility with other systems.
 * Ported from BetterCombat (net.bettercombat.compat.CompatFeatures).
 */
public class CompatibilityFlags {
    /** Whether the player is currently casting a spell (from SpellEngine). */
    public static boolean isPlayerCastingSpell = false;

    /** Whether to show weapon trail particles. */
    public static boolean showTrailParticles = true;
}
