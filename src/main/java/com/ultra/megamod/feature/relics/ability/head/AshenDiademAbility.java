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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class AshenDiademAbility {
    public static final List<RelicAbility> ABILITIES = List.of(
        new RelicAbility("Ember Sight", "Gain night vision automatically in dark areas",
            1, RelicAbility.CastType.PASSIVE,
            List.of()),
        new RelicAbility("Infernal Command", "Pacify fire-type mobs with overwhelming debuffs",
            4, RelicAbility.CastType.INSTANTANEOUS,
            List.of(new RelicStat("radius", 6.0, 10.0, RelicStat.ScaleType.ADD, 0.5))),
        new RelicAbility("Pyroclasm", "Unleash a massive fire AOE explosion",
            7, RelicAbility.CastType.INSTANTANEOUS,
            List.of(new RelicStat("damage", 10.0, 20.0, RelicStat.ScaleType.MULTIPLY_BASE, 0.12),
                    new RelicStat("radius", 5.0, 8.0, RelicStat.ScaleType.ADD, 0.5)))
    );

    public static void register() {
        AbilityCastHandler.registerAbility("Ashen Diadem", "Ember Sight", AshenDiademAbility::executeEmberSight);
        AbilityCastHandler.registerAbility("Ashen Diadem", "Infernal Command", AshenDiademAbility::executeInfernalCommand);
        AbilityCastHandler.registerAbility("Ashen Diadem", "Pyroclasm", AshenDiademAbility::executePyroclasm);
    }

    private static void executeEmberSight(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        if (player.tickCount % 100 != 0) return;
        ServerLevel level = (ServerLevel) player.level();
        int lightLevel = level.getMaxLocalRawBrightness(player.blockPosition());
        if (lightLevel < 7) {
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 120, 0, false, false, true));
        }
    }

    private static void executeInfernalCommand(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        double radius = stats[0];
        ServerLevel level = (ServerLevel) player.level();
        AABB area = new AABB(player.blockPosition()).inflate(radius);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area,
            e -> e != player && e.isAlive() && e.fireImmune() && (double) e.distanceTo((Entity) player) <= radius);
        for (LivingEntity target : targets) {
            target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 200, 4, false, true, true));
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 2, false, true, true));
            level.sendParticles((ParticleOptions) ParticleTypes.FLAME,
                target.getX(), target.getY() + 1.0, target.getZ(), 10, 0.3, 0.5, 0.3, 0.03);
            level.sendParticles((ParticleOptions) ParticleTypes.SMOKE,
                target.getX(), target.getY() + 0.5, target.getZ(), 6, 0.2, 0.3, 0.2, 0.01);
        }
        WeaponEffects.ring(level, ParticleTypes.FLAME, player.getX(), player.getY() + 0.5, player.getZ(),
            radius, 16, 2, 0.1);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1.0f, 0.7f);
    }

    private static void executePyroclasm(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        float damage = (float) stats[0];
        double radius = stats[1];
        ServerLevel level = (ServerLevel) player.level();
        AABB area = new AABB(player.blockPosition()).inflate(radius);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area,
            e -> e != player && e.isAlive() && (double) e.distanceTo((Entity) player) <= radius);
        for (LivingEntity target : targets) {
            target.hurt(level.damageSources().onFire(), damage);
            target.igniteForSeconds(4);
            level.sendParticles((ParticleOptions) ParticleTypes.FLAME,
                target.getX(), target.getY() + 0.5, target.getZ(), 10, 0.3, 0.5, 0.3, 0.05);
        }
        // Massive fire shockwave (4 rings)
        WeaponEffects.shockwave(level, ParticleTypes.FLAME, player.getX(), player.getY() + 0.3, player.getZ(),
            radius, 4, 20, 2);
        // Lava particles
        level.sendParticles((ParticleOptions) ParticleTypes.LAVA,
            player.getX(), player.getY() + 0.5, player.getZ(), 20, radius * 0.5, 0.3, radius * 0.5, 0.0);
        // Smoke cloud
        level.sendParticles((ParticleOptions) ParticleTypes.SMOKE,
            player.getX(), player.getY() + 1.5, player.getZ(), 30, radius * 0.4, 1.0, radius * 0.4, 0.03);
        // Fire pillar at center
        WeaponEffects.column(level, ParticleTypes.FLAME, player.getX(), player.getY(), player.getZ(),
            4.0, 10, 3, 0.2);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.5f, 0.6f);
    }
}
