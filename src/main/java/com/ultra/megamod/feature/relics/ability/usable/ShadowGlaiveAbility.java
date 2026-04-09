/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.phys.AABB
 */
package com.ultra.megamod.feature.relics.ability.usable;

import com.ultra.megamod.feature.relics.ability.AbilityCastHandler;
import com.ultra.megamod.feature.relics.data.RelicAbility;
import com.ultra.megamod.feature.relics.data.RelicStat;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

public class ShadowGlaiveAbility {
    public static final List<RelicAbility> ABILITIES = List.of(new RelicAbility("Throw", "Bouncing projectile hitting multiple targets", 1, RelicAbility.CastType.INSTANTANEOUS, List.of(new RelicStat("damage", 4.0, 10.0, RelicStat.ScaleType.MULTIPLY_BASE, 0.12), new RelicStat("bounces", 2.0, 5.0, RelicStat.ScaleType.ADD, 0.5))), new RelicAbility("Saw Mode", "Spin attack dealing AOE damage", 5, RelicAbility.CastType.INSTANTANEOUS, List.of(new RelicStat("damage", 6.0, 14.0, RelicStat.ScaleType.MULTIPLY_BASE, 0.1), new RelicStat("radius", 3.0, 5.0, RelicStat.ScaleType.ADD, 0.3))));

    public static void register() {
        AbilityCastHandler.registerAbility("Shadow Glaive", "Throw", ShadowGlaiveAbility::executeThrow);
        AbilityCastHandler.registerAbility("Shadow Glaive", "Saw Mode", ShadowGlaiveAbility::executeSawMode);
    }

    private static void executeThrow(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        AABB searchArea;
        float damage = (float)stats[0];
        int bounces = (int)stats[1];
        ServerLevel level = (ServerLevel) player.level();
        List<LivingEntity> candidates = level.getEntitiesOfClass(LivingEntity.class, searchArea = new AABB(player.blockPosition()).inflate(16.0), entity -> entity != player && entity.isAlive());
        if (candidates.isEmpty()) {
            return;
        }
        candidates.sort(Comparator.comparingDouble(e -> e.distanceToSqr((Entity)player)));
        HashSet<Integer> hitIds = new HashSet<Integer>();
        LivingEntity currentTarget = candidates.getFirst();
        for (int i = 0; i < bounces && currentTarget != null; ++i) {
            currentTarget.hurt(level.damageSources().playerAttack((Player)player), damage);
            hitIds.add(currentTarget.getId());
            level.playSound(null, currentTarget.getX(), currentTarget.getY(), currentTarget.getZ(), SoundEvents.TRIDENT_HIT, SoundSource.PLAYERS, 1.0f, 1.2f);
            LivingEntity chainFrom = currentTarget;
            AABB chainArea = new AABB(chainFrom.blockPosition()).inflate(8.0);
            List<LivingEntity> nextCandidates = level.getEntitiesOfClass(LivingEntity.class, chainArea, entity -> entity != player && entity.isAlive() && !hitIds.contains(entity.getId()));
            if (nextCandidates.isEmpty()) break;
            nextCandidates.sort(Comparator.comparingDouble(e -> e.distanceToSqr((Entity)chainFrom)));
            currentTarget = nextCandidates.getFirst();
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 1.0f, 0.8f);
    }

    private static void executeSawMode(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        float damage = (float)stats[0];
        double radius = stats[1];
        ServerLevel level = (ServerLevel) player.level();
        AABB area = new AABB(player.blockPosition()).inflate(radius);
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, area, entity -> entity != player && entity.isAlive() && (double)entity.distanceTo((Entity)player) <= radius);
        for (LivingEntity entity2 : entities) {
            double dz;
            entity2.hurt(level.damageSources().playerAttack((Player)player), damage);
            double dx = entity2.getX() - player.getX();
            double dist = Math.sqrt(dx * dx + (dz = entity2.getZ() - player.getZ()) * dz);
            if (!(dist > 0.0)) continue;
            entity2.knockback(0.5, -dx / dist, -dz / dist);
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.5f, 0.6f);
    }
}

