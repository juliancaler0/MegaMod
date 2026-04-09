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
 * Frost Aura: slows nearby hostile mobs every 20 ticks.
 */
public class FrostAuraEffect extends MobEffect {
    public FrostAuraEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x87CEEB);
    }

    @Override
    public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplifier) {
        AABB area = entity.getBoundingBox().inflate(8.0);
        List<Monster> mobs = level.getEntitiesOfClass(Monster.class, area);
        for (Monster mob : mobs) {
            mob.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 1, false, false));
        }
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % 20 == 0;
    }
}
