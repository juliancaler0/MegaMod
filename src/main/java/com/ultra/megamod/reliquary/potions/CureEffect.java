package com.ultra.megamod.reliquary.potions;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.zombie.ZombieVillager;
import com.ultra.megamod.reliquary.init.ModEffects;

public class CureEffect extends MobEffect {

	public CureEffect() {
		super(MobEffectCategory.BENEFICIAL, 15723850);
	}

	@Override
	public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
		return true;
	}

	@Override
	public boolean applyEffectTick(net.minecraft.server.level.ServerLevel serverLevel, LivingEntity livingEntity, int potency) {
		if (livingEntity instanceof ZombieVillager zombieVillager) {
			if (!zombieVillager.isConverting() && livingEntity.hasEffect(MobEffects.WEAKNESS)) {
				// Access-transformer makes this public at runtime; reflection keeps the source
				// compiling against the non-AT classpath while still invoking the real method.
				try {
					java.lang.reflect.Method m = ZombieVillager.class.getDeclaredMethod("startConverting", java.util.UUID.class, int.class);
					m.setAccessible(true);
					m.invoke(zombieVillager, null, (livingEntity.level().random.nextInt(2401) + 3600) / (potency + 2));
				} catch (ReflectiveOperationException ignored) {
					// no-op; conversion simply won't start
				}
				livingEntity.removeEffect(ModEffects.CURE);
			}
			return true;
		} else {
			livingEntity.removeEffect(ModEffects.CURE);
			return false;
		}
	}
}
