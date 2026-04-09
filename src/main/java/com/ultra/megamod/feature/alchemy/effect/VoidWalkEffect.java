package com.ultra.megamod.feature.alchemy.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * Void Walk: no fall damage + levitation control + ender pearl range doubled.
 * Fall damage prevention is handled in AlchemyEffectEvents via LivingFallEvent.
 * Levitation control via slow-fall companion effect.
 */
public class VoidWalkEffect extends MobEffect {
    public VoidWalkEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x4B0082);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return false; // Passive marker effect, handled by events
    }
}
