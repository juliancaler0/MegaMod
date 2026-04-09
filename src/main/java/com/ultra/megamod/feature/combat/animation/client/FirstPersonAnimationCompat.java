package com.ultra.megamod.feature.combat.animation.client;

import com.ultra.megamod.lib.playeranim.core.api.firstPerson.FirstPersonMode;

/**
 * First-person animation mode detection.
 * Ported from BetterCombat (net.bettercombat.client.compat.FirstPersonAnimationCompatibility).
 * In MegaMod we always use THIRD_PERSON_MODEL mode during attacks
 * (renders the 3P player model in 1P view for visible attack animations).
 */
public class FirstPersonAnimationCompat {

    public static FirstPersonMode firstPersonMode() {
        // Always use third-person model rendering during attacks
        // This makes the player see their own body animate when swinging
        return FirstPersonMode.THIRD_PERSON_MODEL;
    }
}
