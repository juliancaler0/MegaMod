package com.ultra.megamod.feature.alchemy.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * Eagle Eye: +50% ranged damage + projectiles glow + no arrow gravity.
 * Ranged damage boost applied in AlchemyEffectEvents via LivingHurtEvent for projectile sources.
 * Arrow gravity and glow handled in AlchemyEffectEvents via ProjectileImpactEvent / EntityJoinLevelEvent.
 */
public class EagleEyeEffect extends MobEffect {
    public EagleEyeEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x00CED1);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return false; // Passive marker
    }
}
