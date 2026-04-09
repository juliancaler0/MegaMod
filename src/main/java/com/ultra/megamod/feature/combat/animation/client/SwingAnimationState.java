package com.ultra.megamod.feature.combat.animation.client;

import com.ultra.megamod.feature.combat.animation.WeaponAttributes.SwingDirection;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client-side state tracker for active weapon swing animations.
 * <p>
 * When the server broadcasts an {@code AttackAnimationPayload}, the client network handler
 * calls {@link #startSwing} to register an animation for the attacking entity.
 * The renderer ({@link SwingAnimationRenderer}) and particle system ({@link SwingParticleRenderer})
 * query this state each frame to drive visual feedback.
 * <p>
 * Animations are time-based (System.currentTimeMillis) so they remain smooth regardless
 * of tick rate fluctuations. Expired swings are pruned every client tick.
 */
public class SwingAnimationState {

    /** Map of entity ID to their currently-active swing animation data. */
    private static final Map<Integer, ActiveSwing> activeSwings = new ConcurrentHashMap<>();

    /**
     * Snapshot of an in-progress swing animation for one entity.
     *
     * @param direction   The visual swing arc direction.
     * @param isOffHand   True if the off-hand (left arm) is performing the swing.
     * @param twoHanded   True if the weapon is two-handed (both arms move together).
     * @param comboIndex  Position within the weapon's combo sequence (0-based).
     * @param startTimeMs System.currentTimeMillis when the swing began.
     * @param durationMs  Total animation duration in milliseconds (typically 300-500ms).
     */
    public record ActiveSwing(
            SwingDirection direction,
            boolean isOffHand,
            boolean twoHanded,
            int comboIndex,
            long startTimeMs,
            int durationMs
    ) {
        /**
         * Returns the current animation progress as a 0.0-1.0 value.
         * Values beyond 1.0 indicate the animation has completed.
         */
        public float progress() {
            long elapsed = System.currentTimeMillis() - startTimeMs;
            return Math.min((float) elapsed / durationMs, 1.0f);
        }

        /** Returns true if the animation has finished playing. */
        public boolean isExpired() {
            return System.currentTimeMillis() - startTimeMs > durationMs;
        }
    }

    // ── Animation phase boundaries ────────────────────────────────────────

    /** End of wind-up phase (0.0 to this value). */
    public static final float WINDUP_END = 0.3f;
    /** End of attack phase (WINDUP_END to this value). */
    public static final float ATTACK_END = 0.7f;
    // Recovery phase runs from ATTACK_END to 1.0.

    // ── Public API ────────────────────────────────────────────────────────

    /**
     * Called by the network handler when an {@code AttackAnimationPayload} arrives.
     * Starts (or replaces) a swing animation for the given entity.
     *
     * @param entityId  The entity performing the attack.
     * @param direction The direction of the swing arc.
     * @param offHand   True if the attack is with the off-hand.
     * @param twoHanded True if the weapon is two-handed.
     * @param combo     The current combo index.
     */
    public static void startSwing(int entityId, SwingDirection direction,
                                  boolean offHand, boolean twoHanded, int combo) {
        // Duration varies slightly by swing type for feel.
        int durationMs = switch (direction) {
            case STAB -> 320;
            case SLASH_DOWN -> 380;
            case UPPERCUT -> 400;
            case SPIN -> 500;
            case SLASH_RIGHT, SLASH_LEFT -> 350;
        };

        activeSwings.put(entityId, new ActiveSwing(
                direction, offHand, twoHanded, combo,
                System.currentTimeMillis(), durationMs
        ));
    }

    /**
     * Returns the current swing progress for an entity, or -1 if no active swing.
     */
    public static float getSwingProgress(int entityId) {
        ActiveSwing swing = activeSwings.get(entityId);
        if (swing == null) return -1f;
        return swing.progress();
    }

    /**
     * Returns the active swing for an entity, or null if none is active.
     */
    public static ActiveSwing getActiveSwing(int entityId) {
        return activeSwings.get(entityId);
    }

    /**
     * Returns true if the given entity currently has an active (non-expired) swing.
     */
    public static boolean hasActiveSwing(int entityId) {
        ActiveSwing swing = activeSwings.get(entityId);
        return swing != null && !swing.isExpired();
    }

    /**
     * Removes the swing for a specific entity (e.g. when the entity is removed from the world).
     */
    public static void clearSwing(int entityId) {
        activeSwings.remove(entityId);
    }

    /**
     * Called every client tick to prune expired animations from the map.
     * Keeps memory tidy when many entities are attacking.
     */
    public static void tick() {
        activeSwings.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }

    /**
     * Removes all tracked swings. Called on world unload / disconnect.
     */
    public static void clearAll() {
        activeSwings.clear();
    }
}
