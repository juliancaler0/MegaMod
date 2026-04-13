package com.ultra.megamod.lib.spellengine.api.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * Just a plain status effect, that has accessible constructor.
 */
public class CustomStatusEffect extends MobEffect {
    public CustomStatusEffect(MobEffectCategory category, int color) {
        super(category, color);
    }
}
