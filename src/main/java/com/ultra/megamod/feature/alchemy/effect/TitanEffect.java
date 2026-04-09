package com.ultra.megamod.feature.alchemy.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * Titan: Grants extended melee reach and increased attack damage.
 * Reach extension handled via AlchemyEffects event. Strength applied as vanilla effect.
 */
public class TitanEffect extends MobEffect {
    public TitanEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xCC7720);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return false;
    }
}
