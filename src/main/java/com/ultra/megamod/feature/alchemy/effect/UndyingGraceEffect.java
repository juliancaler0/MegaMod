package com.ultra.megamod.feature.alchemy.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * Undying Grace: totem of undying effect triggers once within duration on lethal damage.
 * Handled in AlchemyEffectEvents via LivingDeathEvent — cancels death, applies totem effects.
 */
public class UndyingGraceEffect extends MobEffect {
    public UndyingGraceEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xFFD700);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return false; // Passive marker
    }
}
