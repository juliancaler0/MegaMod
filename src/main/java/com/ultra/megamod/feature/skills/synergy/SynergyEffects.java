package com.ultra.megamod.feature.skills.synergy;

import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

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

    /** Returns true if the player has the Treasure Alchemist synergy active. */
    public static boolean hasTreasureAlchemist(ServerPlayer player) {
        return false; // TODO: implement when synergy system is built
    }

    /** Returns true if the player's Fortune's Favor synergy triggers a double rare drop. */
    public static boolean shouldDoubleRareDrop(ServerPlayer player) {
        return false; // TODO: implement when synergy system is built
    }

    /** Returns the growth speed multiplier from Nature's Harmony synergy (1.0 = no bonus). */
    public static float getGrowthSpeedMultiplier(ServerPlayer player) {
        return 1.0f; // TODO: implement when synergy system is built
    }

    /** Returns the familiar damage multiplier from Spirit Conductor synergy (1.0 = no bonus). */
    public static float getFamiliarDamageMultiplier(ServerPlayer player) {
        return 1.0f; // TODO: implement when synergy system is built
    }

    /** Clears cached synergy data for a player on logout. */
    public static void clearPlayer(UUID playerId) {
        // TODO: implement when synergy system has per-player caches
    }
}
