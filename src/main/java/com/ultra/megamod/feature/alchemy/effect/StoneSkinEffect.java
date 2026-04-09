package com.ultra.megamod.feature.alchemy.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * Stone Skin: Resistance III + Slowness I + knockback immunity.
 * Resistance and Slowness are applied as companion effects.
 * Knockback immunity handled in AlchemyEffectEvents via LivingKnockBackEvent.
 */
public class StoneSkinEffect extends MobEffect {
    public StoneSkinEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x808080);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return false; // Passive marker, handled by events
    }
}
