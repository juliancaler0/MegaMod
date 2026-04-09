package com.ultra.megamod.feature.combat.animation.client;

import com.ultra.megamod.lib.playeranim.core.api.firstPerson.FirstPersonConfiguration;

/**
 * Utility for mirroring first-person arm/item visibility config for off-hand attacks.
 * Ported 1:1 from BetterCombat (net.bettercombat.client.animation.FirstPersonHelper).
 */
public class FirstPersonHelper {
    public static FirstPersonConfiguration mirrored(FirstPersonConfiguration config) {
        return new FirstPersonConfiguration(
                config.isShowLeftArm(), config.isShowRightArm(),
                config.isShowLeftItem(), config.isShowRightItem()
        );
    }
}
