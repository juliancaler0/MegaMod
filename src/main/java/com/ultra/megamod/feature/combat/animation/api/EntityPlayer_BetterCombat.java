package com.ultra.megamod.feature.combat.animation.api;

import com.ultra.megamod.feature.combat.animation.AttackHand;

import javax.annotation.Nullable;

/**
 * Mixin interface for PlayerEntity to expose current attack state.
 * Ported 1:1 from BetterCombat (net.bettercombat.api.EntityPlayer_BetterCombat).
 */
public interface EntityPlayer_BetterCombat {
    @Nullable
    AttackHand getCurrentAttack();

    String getMainHandIdleAnimation();
    String getOffHandIdleAnimation();
}
