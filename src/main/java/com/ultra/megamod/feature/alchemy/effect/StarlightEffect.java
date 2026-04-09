package com.ultra.megamod.feature.alchemy.effect;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * Starlight: night vision + glowing mobs in 32 block radius + luck.
 * Night vision and luck are applied as companion effects.
 * This tick applies Glowing to nearby mobs every 40 ticks.
 */
public class StarlightEffect extends MobEffect {
    public StarlightEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xFFD700);
    }

    @Override
    public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplifier) {
        AABB area = entity.getBoundingBox().inflate(32.0);
        List<LivingEntity> nearby = level.getEntitiesOfClass(LivingEntity.class, area,
                e -> e != entity && e instanceof Monster);
        for (LivingEntity mob : nearby) {
            mob.addEffect(new MobEffectInstance(MobEffects.GLOWING, 60, 0, false, false));
        }
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % 40 == 0;
    }
}
