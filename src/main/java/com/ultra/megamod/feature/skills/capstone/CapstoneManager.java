package com.ultra.megamod.feature.skills.capstone;

import com.ultra.megamod.feature.skills.SkillManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Static utility class that manages capstone ability cooldowns and node-unlock checks.
 * All capstone handlers delegate to this class for cooldown tracking and node validation.
 */
public final class CapstoneManager {

    /** player UUID -> (capstone_id -> last_triggered_tick) */
    private static final Map<UUID, Map<String, Long>> COOLDOWNS = new HashMap<>();

    private CapstoneManager() {}

    /**
     * Checks whether a player has the given tier 4 node unlocked.
     */
    public static boolean hasCapstoneTrigger(ServerPlayer player, String nodeId) {
        ServerLevel level = (ServerLevel) player.level();
        return SkillManager.get(level).isNodeUnlocked(player.getUUID(), nodeId);
    }

    /**
     * Returns true if the capstone ability is still on cooldown.
     */
    public static boolean isOnCooldown(UUID playerId, String capstoneId, long currentTick, long cooldownTicks) {
        Map<String, Long> playerCooldowns = COOLDOWNS.get(playerId);
        if (playerCooldowns == null) {
            return false;
        }
        Long lastTrigger = playerCooldowns.get(capstoneId);
        if (lastTrigger == null) {
            return false;
        }
        return (currentTick - lastTrigger) < cooldownTicks;
    }

    /**
     * Records that the capstone ability was triggered at the given tick.
     */
    public static void setCooldown(UUID playerId, String capstoneId, long currentTick) {
        COOLDOWNS.computeIfAbsent(playerId, k -> new HashMap<>()).put(capstoneId, currentTick);
    }

    /**
     * Cleans up cooldown data when a player logs out.
     */
    public static void onPlayerLogout(UUID playerId) {
        COOLDOWNS.remove(playerId);
    }

    /**
     * Clears all cooldown data (e.g. on server shutdown).
     */
    public static void clearAll() {
        COOLDOWNS.clear();
    }
}
