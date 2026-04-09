/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 */
package com.ultra.megamod.feature.relics.ability.hands;

import com.ultra.megamod.feature.relics.ability.AbilityCastHandler;
import com.ultra.megamod.feature.relics.data.RelicAbility;
import com.ultra.megamod.feature.relics.data.RelicStat;
import java.util.List;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class EnderHandAbility {
    public static final List<RelicAbility> ABILITIES = List.of(new RelicAbility("Enderman Peace", "Endermen won't attack you", 1, RelicAbility.CastType.PASSIVE, List.of()), new RelicAbility("Teleport Swap", "Swap positions with targeted entity", 3, RelicAbility.CastType.INSTANTANEOUS, List.of(new RelicStat("range", 12.0, 20.0, RelicStat.ScaleType.ADD, 1.0))));

    public static void register() {
        AbilityCastHandler.registerAbility("Ender Hand", "Enderman Peace", EnderHandAbility::executeEndermanPeace);
        AbilityCastHandler.registerAbility("Ender Hand", "Teleport Swap", EnderHandAbility::executeTeleportSwap);
    }

    private static void executeEndermanPeace(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        if (player.tickCount % 20 != 0) {
            return;
        }
        ServerLevel level = (ServerLevel) player.level();
        AABB area = new AABB(player.blockPosition()).inflate(16.0);
        List<EnderMan> endermen = level.getEntitiesOfClass(EnderMan.class, area, EnderMan::isAlive);
        for (EnderMan enderman : endermen) {
            if (enderman.getTarget() == player) {
                enderman.setTarget(null);
            }
        }
        level.sendParticles((ParticleOptions) ParticleTypes.REVERSE_PORTAL,
                player.getX(), player.getY() + 1.0, player.getZ(), 3, 0.3, 0.5, 0.3, 0.01);
    }

    private static void executeTeleportSwap(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        double range = stats[0];
        ServerLevel level = player.level();
        Vec3 eyePos = player.getEyePosition();
        Vec3 lookVec = player.getLookAngle();
        Vec3 endPos = eyePos.add(lookVec.scale(range));
        AABB searchArea = new AABB(eyePos, endPos).inflate(1.0);
        Entity closestEntity = null;
        double closestDist = range + 1.0;
        for (Entity candidate : level.getEntities((Entity)player, searchArea, entity -> entity.isAlive() && entity.isPickable())) {
            Vec3 normalizedToEntity;
            double dot;
            Vec3 toEntity = candidate.position().add(0.0, (double)candidate.getBbHeight() / 2.0, 0.0).subtract(eyePos);
            double dist = toEntity.length();
            if (dist > range || !((dot = lookVec.dot(normalizedToEntity = toEntity.normalize())) > 0.95) || !(dist < closestDist)) continue;
            closestDist = dist;
            closestEntity = candidate;
        }
        if (closestEntity == null) {
            return;
        }
        double playerX = player.getX();
        double playerY = player.getY();
        double playerZ = player.getZ();
        double targetX = closestEntity.getX();
        double targetY = closestEntity.getY();
        double targetZ = closestEntity.getZ();
        player.teleportTo(targetX, targetY, targetZ);
        closestEntity.teleportTo(playerX, playerY, playerZ);
        player.fallDistance = 0.0;
        closestEntity.fallDistance = 0.0;
    }
}

