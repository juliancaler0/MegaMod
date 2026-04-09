package com.ultra.megamod.feature.relics.ability.head;

import com.ultra.megamod.feature.relics.ability.AbilityCastHandler;
import com.ultra.megamod.feature.relics.ability.AbilitySystem;
import com.ultra.megamod.feature.relics.data.RelicAbility;
import com.ultra.megamod.feature.relics.data.RelicStat;
import com.ultra.megamod.feature.relics.weapons.WeaponEffects;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class StormcallerCircletAbility {
    public static final List<RelicAbility> ABILITIES = List.of(
        new RelicAbility("Static Field", "Periodically zap the nearest mob with electric damage",
            1, RelicAbility.CastType.PASSIVE,
            List.of(new RelicStat("damage", 1.0, 3.0, RelicStat.ScaleType.ADD, 0.3))),
        new RelicAbility("Thunder Strike", "Call down a devastating lightning bolt on a target",
            4, RelicAbility.CastType.INSTANTANEOUS,
            List.of(new RelicStat("damage", 8.0, 15.0, RelicStat.ScaleType.MULTIPLY_BASE, 0.12))),
        new RelicAbility("Tempest Aura", "Toggle a pulsing electric aura that damages nearby mobs",
            7, RelicAbility.CastType.TOGGLE,
            List.of(new RelicStat("radius", 3.0, 5.0, RelicStat.ScaleType.ADD, 0.3)))
    );

    public static void register() {
        AbilityCastHandler.registerAbility("Stormcaller Circlet", "Static Field", StormcallerCircletAbility::executeStaticField);
        AbilityCastHandler.registerAbility("Stormcaller Circlet", "Thunder Strike", StormcallerCircletAbility::executeThunderStrike);
        AbilityCastHandler.registerAbility("Stormcaller Circlet", "Tempest Aura", StormcallerCircletAbility::executeTempestAura);
    }

    private static void executeStaticField(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        if (player.tickCount % 60 != 0) return;
        float damage = (float) stats[0];
        ServerLevel level = (ServerLevel) player.level();
        AABB area = new AABB(player.blockPosition()).inflate(4.0);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area,
            e -> e != player && e.isAlive() && (double) e.distanceTo((Entity) player) <= 4.0);
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
            nearest.hurt(level.damageSources().magic(), damage);
            level.sendParticles((ParticleOptions) ParticleTypes.ELECTRIC_SPARK,
                nearest.getX(), nearest.getY() + 0.5, nearest.getZ(), 12, 0.3, 0.5, 0.3, 0.05);
            level.sendParticles((ParticleOptions) ParticleTypes.FLASH,
                nearest.getX(), nearest.getY() + 1.0, nearest.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    private static void executeThunderStrike(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        float damage = (float) stats[0];
        ServerLevel level = (ServerLevel) player.level();
        // Find nearest mob within 12 blocks
        AABB area = new AABB(player.blockPosition()).inflate(12.0);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area,
            e -> e != player && e.isAlive() && (double) e.distanceTo((Entity) player) <= 12.0);
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
            target.hurt(level.damageSources().magic(), damage);
            // Lightning column from sky
            WeaponEffects.column(level, ParticleTypes.ELECTRIC_SPARK, target.getX(), target.getY(), target.getZ(),
                10.0, 20, 3, 0.15);
            // Flash at impact
            level.sendParticles((ParticleOptions) ParticleTypes.FLASH,
                target.getX(), target.getY() + 1.0, target.getZ(), 2, 0.0, 0.0, 0.0, 0.0);
            // Spark burst at target
            level.sendParticles((ParticleOptions) ParticleTypes.ELECTRIC_SPARK,
                target.getX(), target.getY() + 0.5, target.getZ(), 20, 0.5, 0.8, 0.5, 0.1);
            // Ring at impact
            WeaponEffects.ring(level, ParticleTypes.ELECTRIC_SPARK, target.getX(), target.getY() + 0.2, target.getZ(),
                2.0, 12, 2, 0.05);
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    private static void executeTempestAura(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        if (!AbilitySystem.isToggleActive(player.getUUID(), "Tempest Aura")) return;
        if (player.tickCount % 20 != 0) return;
        double radius = stats[0];
        ServerLevel level = (ServerLevel) player.level();
        AABB area = new AABB(player.blockPosition()).inflate(radius);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area,
            e -> e != player && e.isAlive() && (double) e.distanceTo((Entity) player) <= radius);
        for (LivingEntity target : targets) {
            target.hurt(level.damageSources().magic(), 1.0f);
            level.sendParticles((ParticleOptions) ParticleTypes.ELECTRIC_SPARK,
                target.getX(), target.getY() + 0.5, target.getZ(), 4, 0.2, 0.3, 0.2, 0.02);
        }
        WeaponEffects.ring(level, ParticleTypes.ELECTRIC_SPARK, player.getX(), player.getY() + 0.5, player.getZ(),
            radius, 12, 1, 0.08);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.PLAYERS, 0.3f, 1.5f);
    }
}
