package com.ultra.megamod.feature.relics.ability.hands_right;

import com.ultra.megamod.feature.relics.ability.AbilityCastHandler;
import com.ultra.megamod.feature.relics.data.RelicAbility;
import com.ultra.megamod.feature.relics.data.RelicStat;
import com.ultra.megamod.feature.relics.weapons.WeaponEffects;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class PlagueGraspAbility {
    public static final List<RelicAbility> ABILITIES = List.of(
        new RelicAbility("Venomous Touch", "Periodically poison the nearest mob within range",
            1, RelicAbility.CastType.PASSIVE,
            List.of(new RelicStat("range", 2.0, 4.0, RelicStat.ScaleType.ADD, 0.3))),
        new RelicAbility("Wither Grip", "Apply a devastating Wither effect to your target",
            4, RelicAbility.CastType.INSTANTANEOUS,
            List.of(new RelicStat("duration", 60.0, 120.0, RelicStat.ScaleType.ADD, 10.0))),
        new RelicAbility("Plague Wave", "Release a toxic cone of poison in front of you",
            7, RelicAbility.CastType.INSTANTANEOUS,
            List.of(new RelicStat("damage", 4.0, 8.0, RelicStat.ScaleType.MULTIPLY_BASE, 0.1),
                    new RelicStat("radius", 4.0, 6.0, RelicStat.ScaleType.ADD, 0.3)))
    );

    public static void register() {
        AbilityCastHandler.registerAbility("Plague Grasp", "Venomous Touch", PlagueGraspAbility::executeVenomousTouch);
        AbilityCastHandler.registerAbility("Plague Grasp", "Wither Grip", PlagueGraspAbility::executeWitherGrip);
        AbilityCastHandler.registerAbility("Plague Grasp", "Plague Wave", PlagueGraspAbility::executePlagueWave);
    }

    private static void executeVenomousTouch(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        if (player.tickCount % 60 != 0) return;
        double range = stats[0];
        ServerLevel level = (ServerLevel) player.level();
        AABB area = new AABB(player.blockPosition()).inflate(range);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area,
            e -> e != player && e.isAlive() && (double) e.distanceTo((Entity) player) <= range);
        // Find nearest
        LivingEntity nearest = null;
        double closestDist = Double.MAX_VALUE;
        for (LivingEntity target : targets) {
            double dist = target.distanceTo((Entity) player);
            if (dist < closestDist) {
                closestDist = dist;
                nearest = target;
            }
        }
        if (nearest != null) {
            nearest.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 0, false, true, true));
            level.sendParticles((ParticleOptions) ParticleTypes.ITEM_SLIME,
                nearest.getX(), nearest.getY() + 0.5, nearest.getZ(), 8, 0.3, 0.4, 0.3, 0.02);
        }
    }

    private static void executeWitherGrip(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        int duration = (int) stats[0];
        ServerLevel level = (ServerLevel) player.level();
        AABB area = new AABB(player.blockPosition()).inflate(6.0);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area,
            e -> e != player && e.isAlive() && (double) e.distanceTo((Entity) player) <= 6.0);
        // Find nearest target
        LivingEntity target = null;
        double closestDist = Double.MAX_VALUE;
        for (LivingEntity entity : targets) {
            double dist = entity.distanceTo((Entity) player);
            if (dist < closestDist) {
                closestDist = dist;
                target = entity;
            }
        }
        if (target != null) {
            target.addEffect(new MobEffectInstance(MobEffects.WITHER, duration, 1, false, true, true));
            // Ported ANTI_HEAL — target can't be healed for the duration of the curse
            target.addEffect(new MobEffectInstance(
                com.ultra.megamod.feature.relics.effect.RelicEffectRegistry.ANTI_HEAL, duration, 0, false, true, true));
            // Smoke line to target
            Vec3 from = player.getEyePosition();
            Vec3 to = target.position().add(0, target.getBbHeight() * 0.5, 0);
            WeaponEffects.line(level, ParticleTypes.SMOKE, from, to, 12, 2, 0.05);
            // Soul fire spiral on target
            WeaponEffects.spiral(level, ParticleTypes.SOUL_FIRE_FLAME, target.getX(), target.getY(), target.getZ(),
                1.0, 2.0, 16, 2);
            // Wither impact
            level.sendParticles((ParticleOptions) ParticleTypes.SMOKE,
                target.getX(), target.getY() + 0.5, target.getZ(), 10, 0.3, 0.5, 0.3, 0.02);
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.WITHER_HURT, SoundSource.PLAYERS, 0.8f, 0.8f);
    }

    private static void executePlagueWave(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        float damage = (float) stats[0];
        double radius = stats[1];
        ServerLevel level = (ServerLevel) player.level();
        Vec3 look = player.getLookAngle().normalize();
        AABB area = new AABB(player.blockPosition()).inflate(radius);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area,
            e -> e != player && e.isAlive() && (double) e.distanceTo((Entity) player) <= radius);
        for (LivingEntity target : targets) {
            // Check if target is within cone in front of player
            Vec3 toTarget = target.position().subtract(player.position()).normalize();
            double dot = look.x * toTarget.x + look.z * toTarget.z;
            if (dot > 0.33) { // roughly 70-degree half-angle cone (sweepAngle ~2PI/3)
                target.hurt(level.damageSources().magic(), damage);
                target.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 1, false, true, true));
                target.addEffect(new MobEffectInstance(
                    com.ultra.megamod.feature.relics.effect.RelicEffectRegistry.ANTI_HEAL, 100, 0, false, true, true));
                level.sendParticles((ParticleOptions) ParticleTypes.ITEM_SLIME,
                    target.getX(), target.getY() + 0.5, target.getZ(), 6, 0.3, 0.4, 0.3, 0.02);
            }
        }
        // Poison cone arc
        WeaponEffects.arc(level, ParticleTypes.ITEM_SLIME, player.getX(), player.getY() + 0.5, player.getZ(),
            look.x, look.z, radius, Math.PI * 2.0 / 3.0, 14, 2);
        // Smoke in cone
        WeaponEffects.arc(level, ParticleTypes.SMOKE, player.getX(), player.getY() + 0.8, player.getZ(),
            look.x, look.z, radius * 0.7, Math.PI * 2.0 / 3.0, 10, 1);
        // Additional slime particles
        level.sendParticles((ParticleOptions) ParticleTypes.ITEM_SLIME,
            player.getX() + look.x * 2.0, player.getY() + 0.5, player.getZ() + look.z * 2.0,
            12, radius * 0.3, 0.3, radius * 0.3, 0.02);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.BREWING_STAND_BREW, SoundSource.PLAYERS, 1.0f, 0.7f);
    }
}
