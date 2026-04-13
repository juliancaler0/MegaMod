package com.ultra.megamod.lib.rangedweapon.internal;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class CustomStatusEffect extends MobEffect {
    public static String uuid = "e8222db4-6c3c-4bbe-bacb-6e8d07e96e8b";
    public CustomStatusEffect(MobEffectCategory category, int color) {
        super(category, color);
    }
}
