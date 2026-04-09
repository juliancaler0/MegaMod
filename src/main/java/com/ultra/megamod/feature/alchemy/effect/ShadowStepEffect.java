package com.ultra.megamod.feature.alchemy.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * Shadow Step: invisibility + speed + no footstep sounds.
 * Invisibility and speed are applied as companion effects when the potion is consumed.
 * Footstep silencing is handled in AlchemyEffectEvents.
 */
public class ShadowStepEffect extends MobEffect {
    public ShadowStepEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x2F1B4E);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return false; // Passive marker effect
    }
}
