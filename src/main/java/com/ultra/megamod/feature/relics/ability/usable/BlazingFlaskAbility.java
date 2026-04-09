/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 */
package com.ultra.megamod.feature.relics.ability.usable;

import com.ultra.megamod.feature.relics.ability.AbilityCastHandler;
import com.ultra.megamod.feature.relics.data.RelicAbility;
import com.ultra.megamod.feature.relics.data.RelicStat;
import java.util.List;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class BlazingFlaskAbility {
    public static final List<RelicAbility> ABILITIES = List.of(new RelicAbility("Fire Ball", "Launch a fireball in look direction", 1, RelicAbility.CastType.INSTANTANEOUS, List.of(new RelicStat("damage", 5.0, 12.0, RelicStat.ScaleType.MULTIPLY_BASE, 0.12))), new RelicAbility("Jetpack", "Brief upward thrust with fire", 5, RelicAbility.CastType.INSTANTANEOUS, List.of(new RelicStat("thrust", 1.0, 2.5, RelicStat.ScaleType.ADD, 0.2))));

    public static void register() {
        AbilityCastHandler.registerAbility("Blazing Flask", "Fire Ball", BlazingFlaskAbility::executeFireBall);
        AbilityCastHandler.registerAbility("Blazing Flask", "Jetpack", BlazingFlaskAbility::executeJetpack);
    }

    private static void executeFireBall(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        ServerLevel level = player.level();
        Vec3 look = player.getLookAngle();
        Vec3 eyePos = player.getEyePosition();
        double startX = eyePos.x + look.x * 1.5;
        double startY = eyePos.y + look.y * 1.5;
        double startZ = eyePos.z + look.z * 1.5;
        SmallFireball fireball = new SmallFireball((Level)level, (LivingEntity)player, new Vec3(look.x, look.y, look.z));
        fireball.setPos(startX, startY, startZ);
        level.addFreshEntity((Entity)fireball);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1.0f, 1.0f);
        // Apply scaled damage to entities in front
        float damage = stats.length > 0 ? (float) stats[0] : 5.0f;
        Vec3 start = player.getEyePosition();
        AABB area = new AABB(start.add(look.scale(3)), start.add(look.scale(6))).inflate(2.0);
        for (Entity entity : player.level().getEntities(player, area)) {
            if (entity instanceof LivingEntity living) {
                living.hurt(player.damageSources().playerAttack(player), damage);
                living.setRemainingFireTicks(100);
            }
        }
    }

    private static void executeJetpack(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        double offsetZ;
        double offsetX;
        int i;
        double thrust = stats[0];
        ServerLevel level = player.level();
        Vec3 motion = player.getDeltaMovement();
        player.setDeltaMovement(motion.x, thrust, motion.z);
        player.hurtMarked = true;
        player.fallDistance = 0.0;
        double px = player.getX();
        double py = player.getY();
        double pz = player.getZ();
        for (i = 0; i < 20; ++i) {
            offsetX = (level.random.nextDouble() - 0.5) * 0.6;
            offsetZ = (level.random.nextDouble() - 0.5) * 0.6;
            level.sendParticles((ParticleOptions)ParticleTypes.FLAME, px + offsetX, py, pz + offsetZ, 1, 0.0, -0.1, 0.0, 0.02);
        }
        for (i = 0; i < 10; ++i) {
            offsetX = (level.random.nextDouble() - 0.5) * 0.8;
            offsetZ = (level.random.nextDouble() - 0.5) * 0.8;
            level.sendParticles((ParticleOptions)ParticleTypes.SMOKE, px + offsetX, py - 0.2, pz + offsetZ, 1, 0.0, -0.05, 0.0, 0.01);
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.0f, 0.7f);
    }
}

