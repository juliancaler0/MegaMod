package com.ultra.megamod.feature.combat.animation;

/**
 * Tracks a player's current position within a weapon's combo sequence.
 *
 * @param current The current attack index (0-based) within the filtered attack list.
 * @param total   The total number of available attacks in the combo (after condition filtering).
 */
public record ComboState(int current, int total) {

    /**
     * Advances to the next attack in the combo, wrapping back to the first.
     */
    public ComboState next() {
        return new ComboState((current + 1) % total, total);
    }

    /**
     * Returns true if this is the first attack in the combo sequence.
     */
    public boolean isFirst() {
        return current == 0;
    }

    /**
     * Returns true if this is the last attack in the combo sequence.
     */
    public boolean isLast() {
        return current == total - 1;
    }

    /**
     * Returns the progress through the combo as a 0.0-1.0 value.
     */
    public float progress() {
        return total > 0 ? (float) current / total : 0f;
    }
}
