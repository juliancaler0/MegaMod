package com.ultra.megamod.feature.alchemy.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * Inferno Boost: increases fire-type damage. The actual damage boost is applied
 * via LivingHurtEvent in AlchemyEffectEvents.
 */
public class InfernoBoostEffect extends MobEffect {
    public InfernoBoostEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xFF4500);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return false; // Passive effect, handled by event
    }
}
