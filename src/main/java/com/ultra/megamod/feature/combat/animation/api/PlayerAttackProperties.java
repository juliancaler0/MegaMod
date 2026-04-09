package com.ultra.megamod.feature.combat.animation.api;

/**
 * Mixin interface for Player entities to track combo count.
 * Ported 1:1 from BetterCombat (net.bettercombat.logic.PlayerAttackProperties).
 */
public interface PlayerAttackProperties {
    int getComboCount();
    void setComboCount(int comboCount);
}
