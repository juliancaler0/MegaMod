package com.ultra.megamod.feature.combat.rogues.mixin;

/**
 * Stealth LivingEntity mixin reference.
 * Ported from net.rogues.mixin.LivingEntityStealth.
 *
 * The actual mixin class is at {@link com.ultra.megamod.mixin.rogues.LivingEntityStealth}
 * (must be in the mixin package to be loaded by the mixin subsystem).
 *
 * Functionality:
 * - Hooks updateInvisibilityStatus to treat STEALTH as invisibility
 * - Hooks getVisibilityPercent to reduce detection range for stealthed entities
 */
public class LivingEntityStealth {
    // See com.ultra.megamod.mixin.rogues.LivingEntityStealth
}
