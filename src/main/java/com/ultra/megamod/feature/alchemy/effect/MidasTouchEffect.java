package com.ultra.megamod.feature.alchemy.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * Midas Touch: +100% MegaCoin drops from mobs.
 * Coin drop multiplication handled in AlchemyEffectEvents via LivingDeathEvent on mobs.
 */
public class MidasTouchEffect extends MobEffect {
    public MidasTouchEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xFFD700);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return false; // Passive marker, handled by economy events
    }
}
