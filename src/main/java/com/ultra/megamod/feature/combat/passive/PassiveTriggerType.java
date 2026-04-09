package com.ultra.megamod.feature.combat.passive;

/**
 * When a passive trigger fires.
 */
public enum PassiveTriggerType {
    /** On successful melee hit (via BetterCombat or vanilla attack). */
    MELEE_IMPACT,
    /** When an arrow or crossbow bolt hits a target. */
    ARROW_HIT,
    /** When the player casts any spell. */
    SPELL_CAST,
    /** When the player blocks damage with a shield. */
    ON_BLOCK
}
