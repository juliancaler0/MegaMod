package com.ultra.megamod.feature.skills.synergy;

import net.minecraft.server.level.ServerPlayer;

/**
 * Stub for skill-tree synergy effects. Methods return neutral defaults
 * until the full synergy system is implemented.
 */
public final class SynergyEffects {

    private SynergyEffects() {}

    /** Returns the Runic Arsenal elemental-damage multiplier (1.0 = no bonus). */
    public static float getRunicArsenalMultiplier(ServerPlayer player) {
        return 1.0f;
    }

    /** Returns the lifesteal healing multiplier (1.0 = no bonus). */
    public static float getLifestealMultiplier(ServerPlayer player) {
        return 1.0f;
    }
}
