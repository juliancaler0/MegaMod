package com.ultra.megamod.reliquary.util;

import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

public class EntityHelper {
	public static void removeNegativeStatusEffects(LivingEntity player) {
		player.removeEffect(MobEffects.WITHER);
		player.removeEffect(MobEffects.HUNGER);
		player.removeEffect(MobEffects.POISON);
		player.removeEffect(MobEffects.NAUSEA);
		player.removeEffect(MobEffects.MINING_FATIGUE);
		player.removeEffect(MobEffects.SLOWNESS);
		player.removeEffect(MobEffects.BLINDNESS);
		player.removeEffect(MobEffects.WEAKNESS);
	}
}
