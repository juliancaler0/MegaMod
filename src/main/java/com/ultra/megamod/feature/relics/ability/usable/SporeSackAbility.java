/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.AreaEffectCloud
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 */
package com.ultra.megamod.feature.relics.ability.usable;

import com.ultra.megamod.feature.relics.ability.AbilityCastHandler;
import com.ultra.megamod.feature.relics.data.RelicAbility;
import com.ultra.megamod.feature.relics.data.RelicStat;
import java.util.Comparator;
import java.util.List;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class SporeSackAbility {
    public static final List<RelicAbility> ABILITIES = List.of(new RelicAbility("Spore Shot", "Poison projectile targeting nearest mob", 1, RelicAbility.CastType.INSTANTANEOUS, List.of(new RelicStat("damage", 3.0, 7.0, RelicStat.ScaleType.MULTIPLY_BASE, 0.1), new RelicStat("poison_duration", 3.0, 8.0, RelicStat.ScaleType.ADD, 0.8))), new RelicAbility("Cloud", "Create a lingering poison cloud", 5, RelicAbility.CastType.INSTANTANEOUS, List.of(new RelicStat("radius", 3.0, 6.0, RelicStat.ScaleType.ADD, 0.4), new RelicStat("duration", 5.0, 10.0, RelicStat.ScaleType.ADD, 1.0))));

    public static void register() {
        AbilityCastHandler.registerAbility("Spore Sack", "Spore Shot", SporeSackAbility::executeSporeShot);
        AbilityCastHandler.registerAbility("Spore Sack", "Cloud", SporeSackAbility::executeCloud);
    }

    private static void executeSporeShot(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        AABB searchArea;
        float damage = (float)stats[0];
        int poisonDuration = (int)stats[1];
        ServerLevel level = (ServerLevel) player.level();
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, searchArea = new AABB(player.blockPosition()).inflate(16.0), entity -> entity != player && entity.isAlive());
        if (targets.isEmpty()) {
            player.displayClientMessage(Component.literal("No targets nearby!"), true);
            return;
        }
        targets.sort(Comparator.comparingDouble(e -> e.distanceToSqr((Entity)player)));
        LivingEntity target = targets.getFirst();
        target.hurt(level.damageSources().playerAttack((Player)player), damage);
        target.addEffect(new MobEffectInstance(MobEffects.POISON, poisonDuration * 20, 1, false, true, true));
        Vec3 start = player.getEyePosition();
        Vec3 end = target.position().add(0.0, (double)(target.getBbHeight() / 2.0f), 0.0);
        Vec3 dir = end.subtract(start);
        int particleCount = (int)(dir.length() * 2.0);
        for (int i = 0; i < particleCount; ++i) {
            double t = (double)i / (double)particleCount;
            double px = start.x + dir.x * t;
            double py = start.y + dir.y * t;
            double pz = start.z + dir.z * t;
            level.sendParticles((ParticleOptions)ParticleTypes.SPORE_BLOSSOM_AIR, px, py, pz, 1, 0.1, 0.1, 0.1, 0.0);
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PUFFER_FISH_BLOW_UP, SoundSource.PLAYERS, 1.0f, 0.8f);
    }

    private static void executeCloud(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        float radius = (float)stats[0];
        int durationSeconds = (int)stats[1];
        ServerLevel level = (ServerLevel) player.level();
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 targetPos = eye.add(look.scale(8.0));
        AreaEffectCloud cloud = new AreaEffectCloud(level, targetPos.x, targetPos.y, targetPos.z);
        cloud.setOwner((LivingEntity)player);
        cloud.setRadius(radius);
        cloud.setRadiusOnUse(-0.1f);
        cloud.setRadiusPerTick(-radius / ((float)durationSeconds * 20.0f));
        cloud.setDuration(durationSeconds * 20);
        cloud.setWaitTime(10);
        cloud.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 1, false, true, false));
        cloud.setCustomParticle((ParticleOptions)ParticleTypes.SPORE_BLOSSOM_AIR);
        level.addFreshEntity(cloud);
        level.playSound(null, targetPos.x, targetPos.y, targetPos.z, SoundEvents.PUFFER_FISH_BLOW_OUT, SoundSource.PLAYERS, 1.5f, 0.6f);
    }
}

