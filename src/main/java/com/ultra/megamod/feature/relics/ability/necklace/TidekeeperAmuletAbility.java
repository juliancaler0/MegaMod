package com.ultra.megamod.feature.relics.ability.necklace;

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

public class TidekeeperAmuletAbility {
    public static final List<RelicAbility> ABILITIES = List.of(
            new RelicAbility("Aquatic Grace", "Grants Water Breathing and Dolphin's Grace", 1,
                    RelicAbility.CastType.PASSIVE, List.of()),
            new RelicAbility("Tidal Wave", "Push and damage enemies in a cone", 4,
                    RelicAbility.CastType.INSTANTANEOUS, List.of(
                    new RelicStat("damage", 4.0, 8.0, RelicStat.ScaleType.MULTIPLY_BASE, 0.1),
                    new RelicStat("range", 4.0, 7.0, RelicStat.ScaleType.ADD, 0.5))),
            new RelicAbility("Whirlpool", "Pull enemies into a damaging vortex", 7,
                    RelicAbility.CastType.INSTANTANEOUS, List.of(
                    new RelicStat("damage", 5.0, 10.0, RelicStat.ScaleType.MULTIPLY_BASE, 0.1),
                    new RelicStat("radius", 4.0, 6.0, RelicStat.ScaleType.ADD, 0.3)))
    );

    public static void register() {
        AbilityCastHandler.registerAbility("Tidekeeper Amulet", "Aquatic Grace", TidekeeperAmuletAbility::executeAquaticGrace);
        AbilityCastHandler.registerAbility("Tidekeeper Amulet", "Tidal Wave", TidekeeperAmuletAbility::executeTidalWave);
        AbilityCastHandler.registerAbility("Tidekeeper Amulet", "Whirlpool", TidekeeperAmuletAbility::executeWhirlpool);
    }

    private static void executeAquaticGrace(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        if (player.tickCount % 80 != 0) {
            return;
        }
        player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 100, 0, false, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 100, 0, false, true, true));
        ServerLevel level = (ServerLevel) player.level();
        level.sendParticles((ParticleOptions) ParticleTypes.BUBBLE,
                player.getX(), player.getY() + 1.0, player.getZ(), 4, 0.3, 0.5, 0.3, 0.01);
    }

    private static void executeTidalWave(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        float damage = (float) stats[0];
        double range = stats[1];
        ServerLevel level = (ServerLevel) player.level();
        Vec3 look = player.getLookAngle();
        Vec3 eyePos = player.getEyePosition();

        AABB searchArea = new AABB(player.blockPosition()).inflate(range);
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, searchArea,
                e -> e != player && e.isAlive());

        for (LivingEntity entity : entities) {
            Vec3 toEntity = entity.position().subtract(player.position()).normalize();
            double dot = look.x * toEntity.x + look.z * toEntity.z;
            double dist = entity.distanceTo((Entity) player);
            if (dot > 0.5 && dist <= range) {
                entity.hurt(level.damageSources().magic(), damage);
                entity.knockback(2.0, -look.x, -look.z);
            }
        }

        Vec3 end = eyePos.add(look.scale(range));
        Vec3 perpX = new Vec3(-look.z, 0, look.x).normalize();
        WeaponEffects.line(level, (ParticleOptions) ParticleTypes.SPLASH, eyePos, end, 20, 2, 0.1);
        WeaponEffects.line(level, (ParticleOptions) ParticleTypes.SPLASH,
                eyePos.add(perpX.scale(0.8)), end.add(perpX.scale(0.8)), 20, 1, 0.1);
        WeaponEffects.line(level, (ParticleOptions) ParticleTypes.SPLASH,
                eyePos.add(perpX.scale(-0.8)), end.add(perpX.scale(-0.8)), 20, 1, 0.1);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.PLAYER_SPLASH, SoundSource.PLAYERS, 1.5f, 0.8f);
    }

    private static void executeWhirlpool(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        float damage = (float) stats[0];
        double radius = stats[1];
        ServerLevel level = (ServerLevel) player.level();
        double cx = player.getX();
        double cy = player.getY();
        double cz = player.getZ();

        AABB area = new AABB(player.blockPosition()).inflate(radius);
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, area,
                e -> e != player && e.isAlive() && (double) e.distanceTo((Entity) player) <= radius);

        for (LivingEntity entity : entities) {
            entity.hurt(level.damageSources().magic(), damage);
            double dx = cx - entity.getX();
            double dz = cz - entity.getZ();
            double dist = Math.sqrt(dx * dx + dz * dz);
            if (dist > 0.5) {
                entity.setDeltaMovement(entity.getDeltaMovement().add(dx / dist * 0.8, 0.2, dz / dist * 0.8));
                entity.hurtMarked = true;
            }
        }

        WeaponEffects.ring(level, (ParticleOptions) ParticleTypes.SPLASH, cx, cy + 0.2, cz, radius, 20, 2, 0.05);
        WeaponEffects.ring(level, (ParticleOptions) ParticleTypes.SPLASH, cx, cy + 1.0, cz, radius * 0.7, 16, 1, 0.05);
        WeaponEffects.ring(level, (ParticleOptions) ParticleTypes.SPLASH, cx, cy + 1.8, cz, radius * 0.4, 12, 1, 0.05);
        WeaponEffects.converge(level, (ParticleOptions) ParticleTypes.BUBBLE, cx, cy + 0.5, cz, radius, 16, 2);
        WeaponEffects.spiral(level, (ParticleOptions) ParticleTypes.SNOWFLAKE, cx, cy, cz, radius, 2.5, 24, 1);
        level.playSound(null, cx, cy, cz,
                SoundEvents.AMBIENT_UNDERWATER_ENTER, SoundSource.PLAYERS, 1.5f, 0.6f);
    }
}
