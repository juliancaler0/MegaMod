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
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class SunforgedBracerAbility {
    public static final List<RelicAbility> ABILITIES = List.of(
        new RelicAbility("Blessed Strikes", "Gain Strength when undead mobs are nearby",
            1, RelicAbility.CastType.PASSIVE,
            List.of(new RelicStat("range", 4.0, 8.0, RelicStat.ScaleType.ADD, 0.5))),
        new RelicAbility("Purifying Touch", "Cleanse all negative effects from yourself",
            5, RelicAbility.CastType.INSTANTANEOUS,
            List.of()),
        new RelicAbility("Divine Smite", "Call down a holy beam on the nearest target",
            7, RelicAbility.CastType.INSTANTANEOUS,
            List.of(new RelicStat("damage", 8.0, 15.0, RelicStat.ScaleType.MULTIPLY_BASE, 0.12)))
    );

    public static void register() {
        AbilityCastHandler.registerAbility("Sunforged Bracer", "Blessed Strikes", SunforgedBracerAbility::executeBlessedStrikes);
        AbilityCastHandler.registerAbility("Sunforged Bracer", "Purifying Touch", SunforgedBracerAbility::executePurifyingTouch);
        AbilityCastHandler.registerAbility("Sunforged Bracer", "Divine Smite", SunforgedBracerAbility::executeDivineSmite);
    }

    private static void executeBlessedStrikes(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        if (player.tickCount % 40 != 0) return;
        double range = stats[0];
        ServerLevel level = (ServerLevel) player.level();
        AABB area = new AABB(player.blockPosition()).inflate(range);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area,
            e -> e != player && e.isAlive() && isUndead(e) && (double) e.distanceTo((Entity) player) <= range);
        if (!targets.isEmpty()) {
            player.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 60, 0, false, true, true));
            level.sendParticles((ParticleOptions) ParticleTypes.END_ROD,
                player.getX(), player.getY() + 1.0, player.getZ(), 4, 0.3, 0.5, 0.3, 0.02);
        }
    }

    private static boolean isUndead(LivingEntity entity) {
        return entity instanceof Zombie || entity instanceof AbstractSkeleton
            || entity.getType().getDescriptionId().contains("zombie")
            || entity.getType().getDescriptionId().contains("skeleton")
            || entity.getType().getDescriptionId().contains("wither")
            || entity.getType().getDescriptionId().contains("phantom");
    }

    private static void executePurifyingTouch(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        ServerLevel level = (ServerLevel) player.level();
        // Remove all negative effects
        player.getActiveEffects().stream()
            .filter(effect -> !effect.getEffect().value().isBeneficial())
            .map(effect -> effect.getEffect())
            .toList()
            .forEach(player::removeEffect);
        // Holy spiral
        WeaponEffects.spiral(level, ParticleTypes.HEART, player.getX(), player.getY(), player.getZ(),
            1.5, 2.5, 20, 2);
        WeaponEffects.spiral(level, ParticleTypes.ENCHANT, player.getX(), player.getY(), player.getZ(),
            1.2, 2.0, 16, 2);
        // Burst of light
        level.sendParticles((ParticleOptions) ParticleTypes.END_ROD,
            player.getX(), player.getY() + 1.0, player.getZ(), 15, 0.5, 0.8, 0.5, 0.05);
        level.sendParticles((ParticleOptions) ParticleTypes.HEART,
            player.getX(), player.getY() + 1.5, player.getZ(), 6, 0.4, 0.4, 0.4, 0.0);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0f, 1.2f);
    }

    private static void executeDivineSmite(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        float damage = (float) stats[0];
        ServerLevel level = (ServerLevel) player.level();
        AABB area = new AABB(player.blockPosition()).inflate(12.0);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area,
            e -> e != player && e.isAlive() && (double) e.distanceTo((Entity) player) <= 12.0);
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
            // Bonus damage to undead
            float finalDamage = isUndead(target) ? damage * 1.5f : damage;
            target.hurt(level.damageSources().magic(), finalDamage);
            // Holy beam from sky
            WeaponEffects.column(level, ParticleTypes.END_ROD, target.getX(), target.getY(), target.getZ(),
                12.0, 24, 3, 0.1);
            // Ring at impact
            WeaponEffects.ring(level, ParticleTypes.END_ROD, target.getX(), target.getY() + 0.2, target.getZ(),
                2.0, 14, 2, 0.05);
            // Flash at impact
            level.sendParticles((ParticleOptions) ParticleTypes.FLASH,
                target.getX(), target.getY() + 1.0, target.getZ(), 2, 0.0, 0.0, 0.0, 0.0);
            // Bright burst
            level.sendParticles((ParticleOptions) ParticleTypes.END_ROD,
                target.getX(), target.getY() + 0.5, target.getZ(), 15, 0.4, 0.6, 0.4, 0.08);
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 1.0f, 0.8f);
    }
}
