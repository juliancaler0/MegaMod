package com.ultra.megamod.feature.alchemy.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * Phantom Phase: Spectral form granting invisibility and speed.
 * Actual effects applied via AlchemyPotionUseHandler (vanilla Invisibility + Speed).
 * Custom tick behavior could be added for phasing through non-solid blocks.
 */
public class PhantomPhaseEffect extends MobEffect {
    public PhantomPhaseEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xCCDDFF);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return false;
    }
}
