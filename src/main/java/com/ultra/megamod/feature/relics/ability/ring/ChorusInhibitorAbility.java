/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 */
package com.ultra.megamod.feature.relics.ability.ring;

import com.ultra.megamod.feature.relics.ability.AbilityCastHandler;
import com.ultra.megamod.feature.relics.ability.AbilitySystem;
import com.ultra.megamod.feature.relics.data.RelicAbility;
import com.ultra.megamod.feature.relics.data.RelicStat;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class ChorusInhibitorAbility {
    private static final Set<UUID> ANCHORED_PLAYERS = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private static final java.util.Map<UUID, Vec3> ANCHOR_POSITIONS = new ConcurrentHashMap<>();

    public static boolean isAnchored(UUID playerUUID) {
        return ANCHORED_PLAYERS.contains(playerUUID);
    }

    public static void removeAnchored(UUID playerUUID) {
        ANCHORED_PLAYERS.remove(playerUUID);
        ANCHOR_POSITIONS.remove(playerUUID);
    }

    public static final List<RelicAbility> ABILITIES = List.of(new RelicAbility("Anchor", "Prevents chorus fruit teleportation", 1, RelicAbility.CastType.PASSIVE, List.of()), new RelicAbility("Displace", "Teleport targeted mob to random position", 4, RelicAbility.CastType.INSTANTANEOUS, List.of(new RelicStat("range", 8.0, 15.0, RelicStat.ScaleType.ADD, 1.0))), new RelicAbility("Void Grip", "Suppress nearby mob teleportation", 6, RelicAbility.CastType.TOGGLE, List.of(new RelicStat("radius", 4.0, 8.0, RelicStat.ScaleType.ADD, 0.5))));

    public static void register() {
        AbilityCastHandler.registerAbility("Chorus Inhibitor", "Anchor", ChorusInhibitorAbility::executeAnchor);
        AbilityCastHandler.registerAbility("Chorus Inhibitor", "Displace", ChorusInhibitorAbility::executeDisplace);
        AbilityCastHandler.registerAbility("Chorus Inhibitor", "Void Grip", ChorusInhibitorAbility::executeVoidGrip);
    }

    private static void executeAnchor(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        UUID uuid = player.getUUID();
        ANCHORED_PLAYERS.add(uuid);
        Vec3 lastPos = ANCHOR_POSITIONS.get(uuid);
        Vec3 currentPos = player.position();
        if (lastPos != null && lastPos.distanceTo(currentPos) > 8.0) {
            // Sudden teleport detected (chorus fruit) - snap back
            player.teleportTo(lastPos.x, lastPos.y, lastPos.z);
            player.displayClientMessage(net.minecraft.network.chat.Component.literal("\u00a7b[Anchor] Teleport blocked!"), true);
            ((ServerLevel) player.level()).playSound(null, player.blockPosition(),
                net.minecraft.sounds.SoundEvents.CHORUS_FRUIT_TELEPORT, net.minecraft.sounds.SoundSource.PLAYERS, 0.5f, 0.5f);
        }
        ANCHOR_POSITIONS.put(uuid, currentPos);
    }

    private static void executeDisplace(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        double range = stats[0];
        ServerLevel level = player.level();
        Vec3 eyePos = player.getEyePosition();
        Vec3 lookVec = player.getLookAngle();
        AABB searchArea = new AABB(eyePos, eyePos.add(lookVec.scale(range))).inflate(1.0);
        LivingEntity target = null;
        double closestDist = range + 1.0;
        for (LivingEntity candidate : level.getEntitiesOfClass(LivingEntity.class, searchArea, entity -> entity != player && entity.isAlive() && entity.isPickable())) {
            Vec3 normalizedToEntity;
            double dot;
            Vec3 toEntity = candidate.position().add(0.0, (double)candidate.getBbHeight() / 2.0, 0.0).subtract(eyePos);
            double dist = toEntity.length();
            if (dist > range || !((dot = lookVec.dot(normalizedToEntity = toEntity.normalize())) > 0.9) || !(dist < closestDist)) continue;
            closestDist = dist;
            target = candidate;
        }
        if (target == null) {
            return;
        }
        double displaceDist = 10.0;
        int maxAttempts = 20;
        for (int attempt = 0; attempt < maxAttempts; ++attempt) {
            BlockPos headPos;
            BlockPos abovePos;
            BlockPos checkPos;
            int dy;
            double offsetX = (level.random.nextDouble() - 0.5) * 2.0 * displaceDist;
            double offsetZ = (level.random.nextDouble() - 0.5) * 2.0 * displaceDist;
            double newX = target.getX() + offsetX;
            double newZ = target.getZ() + offsetZ;
            BlockPos newPos = BlockPos.containing((double)newX, (double)target.getY(), (double)newZ);
            for (dy = 0; dy > -10; --dy) {
                checkPos = newPos.offset(0, dy, 0);
                abovePos = checkPos.above();
                headPos = checkPos.above(2);
                if (level.getBlockState(checkPos).isAir() || !level.getBlockState(abovePos).isAir() || !level.getBlockState(headPos).isAir()) continue;
                target.teleportTo(newX, (double)checkPos.getY() + 1.0, newZ);
                target.fallDistance = 0.0;
                return;
            }
            for (dy = 1; dy <= 10; ++dy) {
                checkPos = newPos.offset(0, dy, 0);
                abovePos = checkPos.above();
                headPos = checkPos.above(2);
                if (level.getBlockState(checkPos).isAir() || !level.getBlockState(abovePos).isAir() || !level.getBlockState(headPos).isAir()) continue;
                target.teleportTo(newX, (double)checkPos.getY() + 1.0, newZ);
                target.fallDistance = 0.0;
                return;
            }
        }
    }

    private static void executeVoidGrip(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        if (!AbilitySystem.isToggleActive(player.getUUID(), "Void Grip")) {
            return;
        }
        if (player.tickCount % 20 != 0) {
            return;
        }
        double radius = stats[0];
        ServerLevel level = (ServerLevel) player.level();
        AABB area = new AABB(player.blockPosition()).inflate(radius);
        List<EnderMan> endermen = level.getEntitiesOfClass(EnderMan.class, area, EnderMan::isAlive);
        for (EnderMan enderman : endermen) {
            enderman.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 30, 2, false, false, false));
            enderman.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 30, 0, false, false, false));
        }
        level.sendParticles((ParticleOptions) ParticleTypes.REVERSE_PORTAL,
                player.getX(), player.getY() + 0.5, player.getZ(),
                8, radius * 0.5, 0.5, radius * 0.5, 0.01);
    }
}

