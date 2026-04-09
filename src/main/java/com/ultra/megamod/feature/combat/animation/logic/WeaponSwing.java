package com.ultra.megamod.feature.combat.animation.logic;

import com.ultra.megamod.feature.combat.animation.AttackHand;

/**
 * Tracks an active weapon swing with upswing timing.
 * Ported 1:1 from BetterCombat (net.bettercombat.logic.WeaponSwing).
 */
public record WeaponSwing(AttackHand attackHand, int startedAt, int upswingTicks, float duration) {
    public int ticksLeft(int time) {
        return startedAt + durationTicks() - time;
    }

    public int upswingTicksLeft(int time) {
        return startedAt + upswingTicks - time;
    }

    public int durationTicks() {
        return Math.round(duration);
    }

    public boolean isValid(int time) {
        return time >= startedAt && time <= startedAt + (durationTicks() + 1);
    }
}
