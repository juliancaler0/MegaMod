package com.ultra.megamod.feature.combat.animation;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server-side singleton that tracks per-player combo state for the Better Combat system.
 * <p>
 * Each player has a {@link ComboData} that records their current position in the weapon's
 * combo sequence, the weapon category they are comboing with, and when the last attack
 * occurred. If the player switches weapon categories or exceeds the timeout window the
 * combo resets to the beginning.
 */
public final class PlayerComboTracker {

    /** Maximum ticks between attacks before the combo resets (1.5 seconds). */
    public static final int COMBO_TIMEOUT = 30;

    private static final Map<UUID, ComboData> ACTIVE_COMBOS = new ConcurrentHashMap<>();

    // ── Public API ──────────────────────────────────────────────────────

    /**
     * Returns the current combo index for a player (0-based). If the player has no
     * active combo or the combo has timed out, returns 0.
     */
    public static int getComboIndex(UUID playerId) {
        ComboData data = ACTIVE_COMBOS.get(playerId);
        if (data == null) return 0;
        return data.comboIndex;
    }

    /**
     * Advances the combo for the given player. If the weapon {@code category} has changed
     * since the last attack or the timeout has elapsed, the combo resets to index 1
     * (meaning the first attack was just performed and we are now pointing at the second).
     *
     * @param playerId      UUID of the attacking player
     * @param category      weapon category string (e.g. "sword", "axe", "mace")
     * @param currentTick   the server's current tick count
     * @return the combo index that was <em>used</em> for this attack (before advancing)
     */
    public static int advanceCombo(UUID playerId, String category, long currentTick) {
        ComboData data = ACTIVE_COMBOS.get(playerId);

        // Reset if no data, category changed, or timeout elapsed
        if (data == null
                || !data.weaponCategory.equals(category)
                || (currentTick - data.lastAttackTime) > COMBO_TIMEOUT) {
            ACTIVE_COMBOS.put(playerId, new ComboData(1, currentTick, category));
            return 0; // This attack used index 0
        }

        int usedIndex = data.comboIndex;
        ACTIVE_COMBOS.put(playerId, new ComboData(usedIndex + 1, currentTick, category));
        return usedIndex;
    }

    /**
     * Resets the combo for the given player back to 0 (e.g. on death, dimension change, etc.).
     */
    public static void resetCombo(UUID playerId) {
        ACTIVE_COMBOS.remove(playerId);
    }

    /**
     * Returns the full {@link ComboData} for inspection, or {@code null} if none exists.
     */
    public static ComboData getComboData(UUID playerId) {
        return ACTIVE_COMBOS.get(playerId);
    }

    /**
     * Removes all tracked combos. Called on server shutdown or world unload.
     */
    public static void clearAll() {
        ACTIVE_COMBOS.clear();
    }

    // ── Inner record ────────────────────────────────────────────────────

    /**
     * Immutable snapshot of a player's combo state.
     *
     * @param comboIndex     the <em>next</em> combo index to use (0-based)
     * @param lastAttackTime server tick when the most recent attack occurred
     * @param weaponCategory the weapon category string of the weapon being comboed
     */
    public record ComboData(int comboIndex, long lastAttackTime, String weaponCategory) { }

    private PlayerComboTracker() { }
}
