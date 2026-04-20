package com.ultra.megamod.feature.combat.relics.spell;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * Ported 1:1 from Relics-1.21.1's CustomStatusEffect.
 * Uses NeoForge's MobEffect + MobEffectCategory (vs Yarn's StatusEffect + StatusEffectCategory).
 */
public class CustomStatusEffect extends MobEffect {
    public CustomStatusEffect(MobEffectCategory category, int color) {
        super(category, color);
    }
}
