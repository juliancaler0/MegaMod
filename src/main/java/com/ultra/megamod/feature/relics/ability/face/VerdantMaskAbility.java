package com.ultra.megamod.feature.relics.ability.face;

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
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class VerdantMaskAbility {
    public static final List<RelicAbility> ABILITIES = List.of(
        new RelicAbility("Pollen Shield", "Poison nearby hostile mobs with drifting pollen",
            1, RelicAbility.CastType.PASSIVE,
            List.of(new RelicStat("radius", 3.0, 6.0, RelicStat.ScaleType.ADD, 0.5))),
        new RelicAbility("Thorned Veil", "Periodically poison the nearest mob that recently attacked you",
            4, RelicAbility.CastType.PASSIVE,
            List.of(new RelicStat("reflect_chance", 15.0, 35.0, RelicStat.ScaleType.ADD, 3.0))),
        new RelicAbility("Blossom Burst", "Poison AOE blast that also heals you",
            7, RelicAbility.CastType.INSTANTANEOUS,
            List.of(new RelicStat("damage", 4.0, 8.0, RelicStat.ScaleType.MULTIPLY_BASE, 0.1),
                    new RelicStat("heal", 4.0, 8.0, RelicStat.ScaleType.ADD, 0.5)))
    );

    public static void register() {
        AbilityCastHandler.registerAbility("Verdant Mask", "Pollen Shield", VerdantMaskAbility::executePollenShield);
        AbilityCastHandler.registerAbility("Verdant Mask", "Thorned Veil", VerdantMaskAbility::executeThornedVeil);
        AbilityCastHandler.registerAbility("Verdant Mask", "Blossom Burst", VerdantMaskAbility::executeBlossomBurst);
    }

    private static void executePollenShield(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        if (player.tickCount % 100 != 0) return;
        double radius = stats[0];
        ServerLevel level = (ServerLevel) player.level();
        AABB area = new AABB(player.blockPosition()).inflate(radius);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area,
            e -> e != player && e.isAlive() && e instanceof Monster && (double) e.distanceTo((Entity) player) <= radius);
        for (LivingEntity target : targets) {
            target.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 0, false, true, true));
            level.sendParticles((ParticleOptions) ParticleTypes.COMPOSTER,
                target.getX(), target.getY() + 0.5, target.getZ(), 6, 0.3, 0.3, 0.3, 0.02);
        }
        // Drifting pollen outward
        level.sendParticles((ParticleOptions) ParticleTypes.COMPOSTER,
            player.getX(), player.getY() + 1.0, player.getZ(), 12, radius * 0.5, 0.5, radius * 0.5, 0.01);
    }

    private static void executeThornedVeil(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        if (player.tickCount % 40 != 0) return;
        ServerLevel level = (ServerLevel) player.level();
        AABB area = new AABB(player.blockPosition()).inflate(4.0);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area,
            e -> e != player && e.isAlive() && e instanceof Monster && (double) e.distanceTo((Entity) player) <= 4.0);
        if (!targets.isEmpty()) {
            // Find nearest hostile mob
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
    }

    private static void executeBlossomBurst(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        float damage = (float) stats[0];
        float heal = (float) stats[1];
        ServerLevel level = (ServerLevel) player.level();
        double radius = 5.0;
        AABB area = new AABB(player.blockPosition()).inflate(radius);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area,
            e -> e != player && e.isAlive() && (double) e.distanceTo((Entity) player) <= radius);
        for (LivingEntity target : targets) {
            target.hurt(level.damageSources().magic(), damage);
            target.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 1, false, true, true));
            level.sendParticles((ParticleOptions) ParticleTypes.COMPOSTER,
                target.getX(), target.getY() + 0.5, target.getZ(), 6, 0.3, 0.4, 0.3, 0.02);
        }
        player.heal(heal);
        // Shockwave of pollen
        WeaponEffects.shockwave(level, ParticleTypes.COMPOSTER, player.getX(), player.getY() + 0.3, player.getZ(),
            radius, 3, 16, 2);
        // Hearts on self
        level.sendParticles((ParticleOptions) ParticleTypes.HEART,
            player.getX(), player.getY() + 1.5, player.getZ(), 6, 0.4, 0.4, 0.4, 0.0);
        level.sendParticles((ParticleOptions) ParticleTypes.COMPOSTER,
            player.getX(), player.getY() + 0.5, player.getZ(), 20, 1.5, 0.8, 1.5, 0.03);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.GRASS_BREAK, SoundSource.PLAYERS, 1.0f, 0.8f);
    }
}
