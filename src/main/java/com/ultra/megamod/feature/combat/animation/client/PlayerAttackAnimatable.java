package com.ultra.megamod.feature.combat.animation.client;

import com.ultra.megamod.feature.combat.animation.logic.AnimatedHand;

/**
 * Interface for entities that can play BetterCombat attack animations.
 * Ported 1:1 from BetterCombat (net.bettercombat.client.animation.PlayerAttackAnimatable).
 */
public interface PlayerAttackAnimatable {
    void updateAnimationsOnTick();
    void playAttackAnimation(String name, AnimatedHand hand, float length, float upswing);
    void stopAttackAnimation(float length);
}
