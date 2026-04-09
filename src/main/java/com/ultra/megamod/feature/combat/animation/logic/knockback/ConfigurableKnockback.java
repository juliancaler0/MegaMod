package com.ultra.megamod.feature.combat.animation.logic.knockback;

/**
 * Mixin interface for LivingEntity to set custom knockback multiplier.
 * Ported 1:1 from BetterCombat (net.bettercombat.logic.knockback.ConfigurableKnockback).
 */
public interface ConfigurableKnockback {
    void setKnockbackMultiplier_BetterCombat(float strength);
}
