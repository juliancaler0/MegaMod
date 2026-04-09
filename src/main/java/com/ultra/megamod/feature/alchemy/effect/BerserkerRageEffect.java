package com.ultra.megamod.feature.alchemy.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * Berserker Rage: Strength III + -50% defense, attacks heal 10% damage dealt.
 * Damage boost and defense reduction handled in AlchemyEffectEvents via LivingHurtEvent.
 * Lifesteal handled in AlchemyEffectEvents via LivingDamageEvent.Post.
 */
public class BerserkerRageEffect extends MobEffect {
    public BerserkerRageEffect() {
        super(MobEffectCategory.NEUTRAL, 0x8B0000);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return false; // Passive, handled by events
    }
}
