package com.ultra.megamod.feature.combat.animation;

import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;

/**
 * Defines how a weapon behaves during melee combat.
 * Each weapon type has a combo sequence of attacks with different hitbox shapes,
 * damage multipliers, angles, and swing directions.
 * <p>
 * Ported and simplified from BetterCombat's JSON-driven weapon attribute system
 * into a hardcoded registry (since we control all items).
 */
public record WeaponAttributes(
        double attackRange,     // Absolute range override (0 = use rangeBonus instead)
        double rangeBonus,      // Added to entity interaction range
        boolean twoHanded,
        String category,
        @Nullable String pose,  // Idle weapon hold animation (e.g., "pose_two_handed_sword")
        Attack[] attacks
) {
    /** Backwards-compatible constructor without pose or attackRange. */
    public WeaponAttributes(double rangeBonus, boolean twoHanded, String category, Attack[] attacks) {
        this(0, rangeBonus, twoHanded, category, null, attacks);
    }
    /** Constructor with pose but no attackRange. */
    public WeaponAttributes(double rangeBonus, boolean twoHanded, String category, @Nullable String pose, Attack[] attacks) {
        this(0, rangeBonus, twoHanded, category, pose, attacks);
    }

    /**
     * Creates empty/default attributes (no attacks, no range bonus).
     */
    public static WeaponAttributes empty() {
        return new WeaponAttributes(0, false, "", new Attack[0]);
    }

    /**
     * Returns the number of attacks in the combo sequence.
     */
    public int comboLength() {
        return attacks != null ? attacks.length : 0;
    }

    /**
     * Returns the attack at the given combo index (wraps around).
     */
    @Nullable
    public Attack getAttack(int comboIndex) {
        if (attacks == null || attacks.length == 0) return null;
        return attacks[Math.floorMod(comboIndex, attacks.length)];
    }

    /**
     * Represents a single swing/strike in a weapon's combo sequence.
     */
    public record Attack(
            HitboxShape hitbox,
            double damageMultiplier,
            double angle,
            double upswing,
            SwingDirection swingDirection,
            @Nullable String animation,  // PlayerAnimator animation ID (e.g., "one_handed_slash_horizontal_right")
            @Nullable Condition[] conditions
    ) {
        /** Backwards-compatible constructor without animation. */
        public Attack(HitboxShape hitbox, double damageMultiplier, double angle,
                      double upswing, SwingDirection swingDirection, @Nullable Condition[] conditions) {
            this(hitbox, damageMultiplier, angle, upswing, swingDirection, null, conditions);
        }

        /**
         * Returns true if this attack's conditions are met. If no conditions
         * are specified, the attack is always available.
         */
        public boolean evaluateConditions(boolean isDualWielding, boolean isSameCategory,
                                          boolean isOffHand, boolean isMounted) {
            if (conditions == null || conditions.length == 0) return true;
            return Arrays.stream(conditions).allMatch(c -> evaluateCondition(c, isDualWielding, isSameCategory, isOffHand, isMounted));
        }

        private static boolean evaluateCondition(Condition condition, boolean isDualWielding,
                                                  boolean isSameCategory, boolean isOffHand, boolean isMounted) {
            return switch (condition) {
                case NOT_DUAL_WIELDING -> !isDualWielding;
                case DUAL_WIELDING_ANY -> isDualWielding;
                case DUAL_WIELDING_SAME_CATEGORY -> isDualWielding && isSameCategory;
                case NO_OFFHAND_ITEM -> true; // Can't check without player ref — always pass
                case OFF_HAND_SHIELD -> true; // Can't check without player ref — always pass
                case MAIN_HAND_ONLY -> !isOffHand;
                case OFF_HAND_ONLY -> isOffHand;
                case MOUNTED -> isMounted;
                case NOT_MOUNTED -> !isMounted;
            };
        }
    }

    /**
     * The 3D shape of the attack's hitbox detection zone.
     * <ul>
     *   <li>{@link #FORWARD_BOX} - Narrow forward thrust (stabs, pokes, wand casts)</li>
     *   <li>{@link #HORIZONTAL_PLANE} - Wide horizontal sweep (slashes, spins)</li>
     *   <li>{@link #VERTICAL_PLANE} - Vertical arc (overheads, chops, slams)</li>
     * </ul>
     */
    public enum HitboxShape {
        FORWARD_BOX,
        HORIZONTAL_PLANE,
        VERTICAL_PLANE
    }

    /**
     * Visual swing direction for animation selection.
     */
    public enum SwingDirection {
        SLASH_RIGHT,
        SLASH_LEFT,
        SLASH_DOWN,
        STAB,
        UPPERCUT,
        SPIN
    }

    /**
     * Conditions that must ALL be true for an attack to be available in the combo sequence.
     * Attacks with unmet conditions are skipped during combo resolution.
     */
    public enum Condition {
        NOT_DUAL_WIELDING,
        DUAL_WIELDING_ANY,
        DUAL_WIELDING_SAME_CATEGORY,
        NO_OFFHAND_ITEM,
        OFF_HAND_SHIELD,
        MAIN_HAND_ONLY,
        OFF_HAND_ONLY,
        MOUNTED,
        NOT_MOUNTED
    }

    /**
     * Sound to play with a swing attack.
     * Ported 1:1 from BetterCombat's WeaponAttributes.Sound.
     */
    public record Sound(String id, float volume, float pitch, float randomness) {
        public Sound(String id) { this(id, 1f, 1f, 0.1f); }
    }
}
