package com.ultra.megamod.feature.alchemy.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * Blood Rage: Each hit increases damage by 5% (stacking up to 50%).
 * Player slowly loses HP over time. Handled via events in AlchemyEffects.
 */
public class BloodRageEffect extends MobEffect {
    public BloodRageEffect() {
        super(MobEffectCategory.HARMFUL, 0xAA0020);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        // Tick every 2 seconds (40 ticks) for HP drain
        return duration % 40 == 0;
    }
}
