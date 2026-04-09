package com.ultra.megamod.feature.relics.ability.face;

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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class FrostweaveVeilAbility {
    public static final List<RelicAbility> ABILITIES = List.of(
        new RelicAbility("Cold Breath", "Slow mobs in a frosty arc in front of you",
            1, RelicAbility.CastType.PASSIVE,
            List.of(new RelicStat("range", 3.0, 5.0, RelicStat.ScaleType.ADD, 0.3))),
        new RelicAbility("Frozen Gaze", "Freeze the entity you are looking at with heavy debuffs",
            4, RelicAbility.CastType.INSTANTANEOUS,
            List.of(new RelicStat("duration", 40.0, 80.0, RelicStat.ScaleType.ADD, 5.0))),
        new RelicAbility("Blizzard Aura", "Toggle a frost aura that slows all nearby mobs",
            7, RelicAbility.CastType.TOGGLE,
            List.of(new RelicStat("radius", 4.0, 7.0, RelicStat.ScaleType.ADD, 0.5)))
    );

    public static void register() {
        AbilityCastHandler.registerAbility("Frostweave Veil", "Cold Breath", FrostweaveVeilAbility::executeColdBreath);
        AbilityCastHandler.registerAbility("Frostweave Veil", "Frozen Gaze", FrostweaveVeilAbility::executeFrozenGaze);
        AbilityCastHandler.registerAbility("Frostweave Veil", "Blizzard Aura", FrostweaveVeilAbility::executeBlizzardAura);
    }

    private static void executeColdBreath(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        if (player.tickCount % 30 != 0) return;
        double range = stats[0];
        ServerLevel level = (ServerLevel) player.level();
        Vec3 look = player.getLookAngle().normalize();
        AABB area = new AABB(player.blockPosition()).inflate(range);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area,
            e -> e != player && e.isAlive() && (double) e.distanceTo((Entity) player) <= range);
        for (LivingEntity target : targets) {
            // Check if target is within arc in front of player
            Vec3 toTarget = target.position().subtract(player.position()).normalize();
            double dot = look.x * toTarget.x + look.z * toTarget.z;
            if (dot > 0.5) { // roughly 60-degree half-angle cone
                target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 0, false, true, true));
            }
        }
        WeaponEffects.arc(level, ParticleTypes.SNOWFLAKE, player.getX(), player.getY() + 0.5, player.getZ(),
            look.x, look.z, range, Math.PI / 3.0, 10, 2);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.POWDER_SNOW_STEP, SoundSource.PLAYERS, 0.4f, 1.2f);
    }

    private static void executeFrozenGaze(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        int duration = (int) stats[0];
        ServerLevel level = (ServerLevel) player.level();
        Vec3 eyePos = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();
        double maxDist = 16.0;
        AABB searchArea = new AABB(player.blockPosition()).inflate(maxDist);
        List<LivingEntity> candidates = level.getEntitiesOfClass(LivingEntity.class, searchArea,
            e -> e != player && e.isAlive());
        LivingEntity target = null;
        double closestDist = Double.MAX_VALUE;
        for (LivingEntity entity : candidates) {
            Vec3 toEntity = entity.getEyePosition().subtract(eyePos).normalize();
            double dot = look.dot(toEntity);
            if (dot > 0.96) { // very narrow look angle
                double dist = entity.distanceTo((Entity) player);
                if (dist < closestDist) {
                    closestDist = dist;
                    target = entity;
                }
            }
        }
        if (target != null) {
            target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, duration, 3, false, true, true));
            target.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, duration, 1, false, true, true));
            // Ice line to target
            WeaponEffects.line(level, ParticleTypes.SNOWFLAKE, eyePos,
                target.position().add(0, target.getBbHeight() * 0.5, 0), 12, 2, 0.05);
            // Converge on target
            WeaponEffects.converge(level, ParticleTypes.SNOWFLAKE,
                target.getX(), target.getY() + 0.5, target.getZ(), 1.5, 10, 2);
            level.sendParticles((ParticleOptions) ParticleTypes.BLOCK_CRUMBLE,
                target.getX(), target.getY() + 0.5, target.getZ(), 10, 0.3, 0.4, 0.3, 0.02);
            level.playSound(null, target.getX(), target.getY(), target.getZ(),
                SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.0f, 1.5f);
        }
    }

    private static void executeBlizzardAura(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        if (!AbilitySystem.isToggleActive(player.getUUID(), "Blizzard Aura")) return;
        if (player.tickCount % 20 != 0) return;
        double radius = stats[0];
        ServerLevel level = (ServerLevel) player.level();
        AABB area = new AABB(player.blockPosition()).inflate(radius);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area,
            e -> e != player && e.isAlive() && (double) e.distanceTo((Entity) player) <= radius);
        for (LivingEntity target : targets) {
            target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 0, false, true, true));
        }
        WeaponEffects.ring(level, ParticleTypes.SNOWFLAKE, player.getX(), player.getY() + 0.5, player.getZ(),
            radius, 14, 1, 0.1);
        WeaponEffects.ring(level, ParticleTypes.CLOUD, player.getX(), player.getY() + 1.0, player.getZ(),
            radius * 0.7, 10, 1, 0.08);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.POWDER_SNOW_HIT, SoundSource.PLAYERS, 0.3f, 1.0f);
    }
}
