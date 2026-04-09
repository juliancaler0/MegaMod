package com.ultra.megamod.feature.alchemy.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * Chronos: Slows all nearby mobs in a radius. Handled via tick event in AlchemyEffects.
 */
public class ChronosEffect extends MobEffect {
    public ChronosEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x8850CC);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        // Tick every second (20 ticks) to apply slowness to nearby mobs
        return duration % 20 == 0;
    }
}
