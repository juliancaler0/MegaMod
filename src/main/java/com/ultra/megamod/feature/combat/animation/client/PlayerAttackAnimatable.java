package com.ultra.megamod.feature.combat.animation.client;

import com.ultra.megamod.feature.combat.animation.api.fx.ParticlePlacement;
import com.ultra.megamod.feature.combat.animation.api.fx.TrailAppearance;
import com.ultra.megamod.feature.combat.animation.logic.AnimatedHand;

import java.util.List;

/**
 * Interface for entities that can play BetterCombat attack animations + weapon trails.
 * Ported 1:1 from BetterCombat {@code net.bettercombat.client.animation.PlayerAttackAnimatable}.
 * {@link #playAttackParticles} was previously missing — implementations now receive weapon-trail
 * spawn requests alongside the animation itself.
 */
public interface PlayerAttackAnimatable {
    void updateAnimationsOnTick();
    void playAttackAnimation(String name, AnimatedHand hand, float length, float upswing);
    void playAttackParticles(boolean isOffHand, float weaponRange, int delay,
                             List<ParticlePlacement> particles, TrailAppearance appearance);
    void stopAttackAnimation(float length);
}
