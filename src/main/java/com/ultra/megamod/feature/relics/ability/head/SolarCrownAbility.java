package com.ultra.megamod.feature.relics.ability.head;

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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class SolarCrownAbility {
    public static final List<RelicAbility> ABILITIES = List.of(
        new RelicAbility("Solar Warmth", "Slowly regenerate health during the day",
            1, RelicAbility.CastType.PASSIVE,
            List.of(new RelicStat("regen_interval", 80.0, 40.0, RelicStat.ScaleType.ADD, -4.0))),
        new RelicAbility("Sunfire", "Ignite the ground, dealing fire AOE damage to nearby enemies",
            3, RelicAbility.CastType.INSTANTANEOUS,
            List.of(new RelicStat("damage", 5.0, 9.0, RelicStat.ScaleType.ADD, 0.5),
                    new RelicStat("ignite_duration", 2.0, 5.0, RelicStat.ScaleType.ADD, 0.3))),
        new RelicAbility("Radiance", "Channel solar energy to heal yourself and nearby allies",
            7, RelicAbility.CastType.INSTANTANEOUS,
            List.of(new RelicStat("self_heal", 4.0, 8.0, RelicStat.ScaleType.ADD, 0.5),
                    new RelicStat("ally_heal", 3.0, 5.0, RelicStat.ScaleType.ADD, 0.3)))
    );

    public static void register() {
        AbilityCastHandler.registerAbility("Solar Crown", "Solar Warmth", SolarCrownAbility::executeSolarWarmth);
        AbilityCastHandler.registerAbility("Solar Crown", "Sunfire", SolarCrownAbility::executeSunfire);
        AbilityCastHandler.registerAbility("Solar Crown", "Radiance", SolarCrownAbility::executeRadiance);
    }

    private static void executeSolarWarmth(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        int interval = Math.max(20, (int) stats[0]);
        if (player.tickCount % interval != 0) return;
        ServerLevel level = (ServerLevel) player.level();
        long dayTime = level.getDayTime() % 24000L;
        boolean isDay = dayTime < 13000L || dayTime > 23000L;
        if (isDay && player.getHealth() < player.getMaxHealth()) {
            player.heal(1.0f);
        }
    }

    private static void executeSunfire(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        float damage = (float) stats[0];
        float igniteDuration = (float) stats[1];
        ServerLevel level = (ServerLevel) player.level();
        AABB area = new AABB(player.blockPosition()).inflate(5.0);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area,
            e -> e != player && e.isAlive() && (double) e.distanceTo((Entity) player) <= 5.0);
        for (LivingEntity target : targets) {
            target.hurt(level.damageSources().onFire(), damage);
            target.igniteForSeconds(igniteDuration);
            // Fire burst on each target
            level.sendParticles((ParticleOptions) ParticleTypes.FLAME,
                target.getX(), target.getY() + 0.5, target.getZ(), 10, 0.3, 0.5, 0.3, 0.05);
            level.sendParticles((ParticleOptions) ParticleTypes.LAVA,
                target.getX(), target.getY() + 0.3, target.getZ(), 4, 0.2, 0.2, 0.2, 0.0);
        }
        // Expanding fire shockwave rings
        WeaponEffects.shockwave(level, ParticleTypes.FLAME, player.getX(), player.getY() + 0.3, player.getZ(),
            5.0, 3, 18, 2);
        // Central fire pillar
        WeaponEffects.column(level, ParticleTypes.FLAME, player.getX(), player.getY(), player.getZ(),
            3.0, 8, 3, 0.2);
        // Ember burst
        level.sendParticles((ParticleOptions) ParticleTypes.LAVA,
            player.getX(), player.getY() + 0.5, player.getZ(), 12, 2.0, 0.3, 2.0, 0.0);
        // Smoke ring
        WeaponEffects.ring(level, ParticleTypes.SMOKE, player.getX(), player.getY() + 0.5, player.getZ(),
            4.0, 12, 1, 0.1);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.0f, 0.8f);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 0.5f, 0.6f);
    }

    private static void executeRadiance(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        float selfHeal = (float) stats[0];
        float allyHeal = (float) stats[1];
        ServerLevel level = (ServerLevel) player.level();
        player.heal(selfHeal);
        AABB area = new AABB(player.blockPosition()).inflate(8.0);
        List<ServerPlayer> nearbyPlayers = level.getEntitiesOfClass(ServerPlayer.class, area,
            p -> p != player && p.isAlive() && (double) p.distanceTo((Entity) player) <= 8.0);
        for (ServerPlayer ally : nearbyPlayers) {
            ally.heal(allyHeal);
            // Healing particles on each ally
            level.sendParticles((ParticleOptions) ParticleTypes.HEART,
                ally.getX(), ally.getY() + 1.5, ally.getZ(), 5, 0.3, 0.3, 0.3, 0.0);
            level.sendParticles((ParticleOptions) ParticleTypes.END_ROD,
                ally.getX(), ally.getY() + 0.5, ally.getZ(), 6, 0.3, 0.8, 0.3, 0.03);
        }
        // Healing wave ring expanding outward
        WeaponEffects.shockwave(level, ParticleTypes.END_ROD, player.getX(), player.getY() + 0.5, player.getZ(),
            8.0, 3, 16, 2);
        // Central radiance burst
        WeaponEffects.column(level, ParticleTypes.END_ROD, player.getX(), player.getY(), player.getZ(),
            3.0, 8, 2, 0.1);
        // Hearts at center
        level.sendParticles((ParticleOptions) ParticleTypes.HEART,
            player.getX(), player.getY() + 1.5, player.getZ(), 8, 0.5, 0.5, 0.5, 0.0);
        // Golden sparkle aura
        level.sendParticles((ParticleOptions) ParticleTypes.ENCHANT,
            player.getX(), player.getY() + 0.5, player.getZ(), 15, 1.0, 1.0, 1.0, 0.5);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 1.0f, 1.2f);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.6f, 1.5f);
    }
}
