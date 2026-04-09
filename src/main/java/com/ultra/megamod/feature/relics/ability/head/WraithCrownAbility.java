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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class WraithCrownAbility {
    public static final List<RelicAbility> ABILITIES = List.of(
        new RelicAbility("Ethereal Sight", "Highlight all hostile mobs with Glowing",
            1, RelicAbility.CastType.PASSIVE,
            List.of(new RelicStat("range", 8.0, 16.0, RelicStat.ScaleType.ADD, 1.0))),
        new RelicAbility("Soul Siphon", "Drain health from the nearest target to heal yourself",
            4, RelicAbility.CastType.INSTANTANEOUS,
            List.of(new RelicStat("damage", 4.0, 8.0, RelicStat.ScaleType.MULTIPLY_BASE, 0.1),
                    new RelicStat("heal_percent", 30.0, 60.0, RelicStat.ScaleType.ADD, 5.0))),
        new RelicAbility("Phantom Form", "Toggle spectral form granting resistance at the cost of damage",
            7, RelicAbility.CastType.TOGGLE,
            List.of())
    );

    public static void register() {
        AbilityCastHandler.registerAbility("Wraith Crown", "Ethereal Sight", WraithCrownAbility::executeEtherealSight);
        AbilityCastHandler.registerAbility("Wraith Crown", "Soul Siphon", WraithCrownAbility::executeSoulSiphon);
        AbilityCastHandler.registerAbility("Wraith Crown", "Phantom Form", WraithCrownAbility::executePhantomForm);
    }

    private static void executeEtherealSight(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        if (player.tickCount % 60 != 0) return;
        double range = stats[0];
        ServerLevel level = (ServerLevel) player.level();
        AABB area = new AABB(player.blockPosition()).inflate(range);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area,
            e -> e != player && e.isAlive() && e instanceof Monster && (double) e.distanceTo((Entity) player) <= range);
        for (LivingEntity target : targets) {
            target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 62, 0, false, false, false));
        }
    }

    private static void executeSoulSiphon(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        float damage = (float) stats[0];
        double healPercent = stats[1] / 100.0;
        ServerLevel level = (ServerLevel) player.level();
        AABB area = new AABB(player.blockPosition()).inflate(8.0);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area,
            e -> e != player && e.isAlive() && (double) e.distanceTo((Entity) player) <= 8.0);
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
            target.hurt(level.damageSources().magic(), damage);
            float healAmount = (float) (damage * healPercent);
            player.heal(healAmount);
            // Soul flame line from target to player
            WeaponEffects.line(level, ParticleTypes.SOUL_FIRE_FLAME,
                target.position().add(0, target.getBbHeight() * 0.5, 0),
                player.position().add(0, player.getBbHeight() * 0.5, 0),
                12, 2, 0.05);
            // Hearts on player
            level.sendParticles((ParticleOptions) ParticleTypes.HEART,
                player.getX(), player.getY() + 1.5, player.getZ(), 4, 0.3, 0.3, 0.3, 0.0);
            // Soul particles on target
            level.sendParticles((ParticleOptions) ParticleTypes.SOUL,
                target.getX(), target.getY() + 0.5, target.getZ(), 8, 0.3, 0.5, 0.3, 0.02);
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.WITHER_HURT, SoundSource.PLAYERS, 0.8f, 1.2f);
    }

    private static void executePhantomForm(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        if (!AbilitySystem.isToggleActive(player.getUUID(), "Phantom Form")) return;
        if (player.tickCount % 20 != 0) return;
        ServerLevel level = (ServerLevel) player.level();
        player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 30, 0, false, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 30, 0, false, false, true));
        // Reverse portal aura
        level.sendParticles((ParticleOptions) ParticleTypes.REVERSE_PORTAL,
            player.getX(), player.getY() + 1.0, player.getZ(), 8, 0.4, 0.6, 0.4, 0.02);
        // Soul ring at feet
        WeaponEffects.ring(level, ParticleTypes.SOUL, player.getX(), player.getY() + 0.1, player.getZ(),
            1.2, 10, 1, 0.03);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.PHANTOM_AMBIENT, SoundSource.PLAYERS, 0.2f, 1.5f);
    }
}
