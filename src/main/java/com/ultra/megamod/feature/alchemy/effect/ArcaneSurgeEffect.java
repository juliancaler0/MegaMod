package com.ultra.megamod.feature.alchemy.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * Arcane Surge: +50% ability power, +30% cooldown reduction, mana regen.
 * The actual ability power and cooldown modifiers are read from this effect
 * by the relic/skill systems via hasEffect checks in AlchemyEffectEvents.
 */
public class ArcaneSurgeEffect extends MobEffect {
    public ArcaneSurgeEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x9B59B6);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return false; // Passive marker
    }
}
