/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 */
package com.ultra.megamod.feature.relics.ability.back;

import com.ultra.megamod.feature.relics.ability.AbilityCastHandler;
import com.ultra.megamod.feature.relics.data.RelicAbility;
import com.ultra.megamod.feature.relics.data.RelicStat;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class MidnightRobeAbility {
    public static final List<RelicAbility> ABILITIES = List.of(new RelicAbility("Vanish", "Become invisible in darkness", 1, RelicAbility.CastType.TOGGLE, List.of(new RelicStat("light_threshold", 3.0, 6.0, RelicStat.ScaleType.ADD, 0.5))), new RelicAbility("Backstab", "Bonus damage to mobs facing away", 4, RelicAbility.CastType.INSTANTANEOUS, List.of(new RelicStat("bonus_damage", 4.0, 10.0, RelicStat.ScaleType.MULTIPLY_BASE, 0.15))), new RelicAbility("Shadow Step", "Short-range teleport in look direction", 7, RelicAbility.CastType.INSTANTANEOUS, List.of(new RelicStat("range", 6.0, 12.0, RelicStat.ScaleType.ADD, 1.0))));

    public static void register() {
        AbilityCastHandler.registerAbility("Midnight Robe", "Vanish", MidnightRobeAbility::executeVanish);
        AbilityCastHandler.registerAbility("Midnight Robe", "Backstab", MidnightRobeAbility::executeBackstab);
        AbilityCastHandler.registerAbility("Midnight Robe", "Shadow Step", MidnightRobeAbility::executeShadowStep);
    }

    private static void executeVanish(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        BlockPos pos;
        int lightThreshold = (int)stats[0];
        ServerLevel level = (ServerLevel) player.level();
        int lightLevel = level.getMaxLocalRawBrightness(pos = player.blockPosition());
        if (lightLevel <= lightThreshold) {
            player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 30, 0, false, false, true));
        }
    }

    private static void executeBackstab(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        double bonusDamage = stats[0];
        ServerLevel level = (ServerLevel) player.level();
        AABB area = new AABB(player.blockPosition()).inflate(4.0);
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, area, e -> e != player && e.isAlive());
        LivingEntity target = null;
        double closestDist = Double.MAX_VALUE;
        for (LivingEntity entity : entities) {
            Vec3 entityToPlayer;
            Vec3 entityLook;
            double dot;
            double dist = entity.distanceTo((Entity)player);
            if (dist >= closestDist || !((dot = (entityLook = entity.getLookAngle().normalize()).dot(entityToPlayer = player.position().subtract(entity.position()).normalize())) < 0.0)) continue;
            target = entity;
            closestDist = dist;
        }
        if (target != null) {
            target.hurt(player.damageSources().playerAttack((Player)player), (float)bonusDamage);
        }
    }

    private static void executeShadowStep(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        Vec3 start;
        double range = stats[0];
        ServerLevel level = (ServerLevel) player.level();
        Vec3 look = player.getLookAngle().normalize();
        Vec3 targetPos = start = player.position();
        for (double d = 1.0; d <= range; d += 0.5) {
            BlockPos blockPos;
            Vec3 candidate = start.add(look.scale(d));
            BlockPos feetPos = blockPos = BlockPos.containing((double)candidate.x, (double)candidate.y, (double)candidate.z);
            BlockPos headPos = feetPos.above();
            boolean feetClear = level.getBlockState(feetPos).getCollisionShape((BlockGetter)level, feetPos).isEmpty();
            boolean headClear = level.getBlockState(headPos).getCollisionShape((BlockGetter)level, headPos).isEmpty();
            if (!feetClear || !headClear) break;
            targetPos = candidate;
        }
        if (targetPos.distanceTo(start) > 0.5) {
            player.teleportTo(targetPos.x, targetPos.y, targetPos.z);
            player.fallDistance = 0.0;
            player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 5, 0, false, false, false));
        }
    }
}

