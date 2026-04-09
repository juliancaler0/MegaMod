package com.ultra.megamod.feature.alchemy.effect;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * Tempest: every 60 ticks (3 seconds), strike lightning near a random hostile mob within 16 blocks.
 */
public class TempestEffect extends MobEffect {
    public TempestEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xFFFF00);
    }

    @Override
    public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplifier) {
        AABB area = entity.getBoundingBox().inflate(16.0);
        List<Monster> mobs = level.getEntitiesOfClass(Monster.class, area);
        if (!mobs.isEmpty()) {
            Monster target = mobs.get(level.random.nextInt(mobs.size()));
            LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level, EntitySpawnReason.TRIGGERED);
            if (bolt != null) {
                bolt.setPos(target.getX(), target.getY(), target.getZ());
                bolt.setVisualOnly(false);
                level.addFreshEntity(bolt);
            }
        }
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % 60 == 0;
    }
}
