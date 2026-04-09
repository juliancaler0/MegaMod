package com.ultra.megamod.feature.combat.animation.client;

/**
 * Client-side combo index tracker for immediate animation feedback.
 * <p>
 * When the player attacks with a BetterCombat weapon, the client needs to know which
 * attack in the combo sequence to animate <em>before</em> the server responds. This
 * tracker mirrors the server's {@link com.ultra.megamod.feature.combat.animation.PlayerComboTracker}
 * logic with the same timeout and category-reset rules.
 * <p>
 * The server's combo tracker is authoritative for damage calculations. This client-side
 * tracker only drives the local animation prediction. When the server broadcasts the
 * {@link com.ultra.megamod.feature.combat.animation.AttackAnimationPayload}, the client
 * animation state is overwritten with the server's definitive combo index.
 */
public final class ClientComboTracker {

    /** Maximum time between attacks before the combo resets (matches server: 1.5 seconds = 30 ticks). */
    private static final long COMBO_TIMEOUT_MS = 1500;

    /** Current combo index (0-based). */
    private static int comboIndex = 0;

    /** Weapon category of the current combo. */
    private static String currentCategory = "";

    /** System.currentTimeMillis() of the last attack. */
    private static long lastAttackTime = 0;

    /**
     * Returns the current combo index and advances it for the next call.
     * Resets the combo if the category changed or the timeout elapsed.
     *
     * @param category the weapon category being used (e.g. "sword", "axe")
     * @return the combo index to use for this attack (before advancing)
     */
    public static int getAndAdvance(String category) {
        long now = System.currentTimeMillis();

        // Reset if category changed or timeout elapsed
        if (!category.equals(currentCategory) || (now - lastAttackTime) > COMBO_TIMEOUT_MS) {
            comboIndex = 0;
            currentCategory = category;
        }

        int usedIndex = comboIndex;
        comboIndex++;
        lastAttackTime = now;
        return usedIndex;
    }

    /**
     * Resets the client combo state. Called on world unload / disconnect.
     */
    public static void reset() {
        comboIndex = 0;
        currentCategory = "";
        lastAttackTime = 0;
    }

    private ClientComboTracker() {}
}
