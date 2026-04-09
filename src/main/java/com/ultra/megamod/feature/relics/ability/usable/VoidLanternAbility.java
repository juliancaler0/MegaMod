package com.ultra.megamod.feature.relics.ability.usable;

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
import net.minecraft.world.phys.Vec3;
import java.util.List;

public class VoidLanternAbility {
    public static final List<RelicAbility> ABILITIES = List.of(
            new RelicAbility("Dark Beacon", "Place a gravity well that pulls and damages mobs", 1,
                    RelicAbility.CastType.INSTANTANEOUS, List.of(
                    new RelicStat("damage", 3.0, 6.0, RelicStat.ScaleType.MULTIPLY_BASE, 0.1),
                    new RelicStat("radius", 4.0, 6.0, RelicStat.ScaleType.ADD, 0.3))),
            new RelicAbility("Dimensional Tear", "Rip a portal dealing heavy AOE damage", 5,
                    RelicAbility.CastType.INSTANTANEOUS, List.of(
                    new RelicStat("damage", 6.0, 12.0, RelicStat.ScaleType.MULTIPLY_BASE, 0.12),
                    new RelicStat("radius", 3.0, 5.0, RelicStat.ScaleType.ADD, 0.3)))
    );

    public static void register() {
        AbilityCastHandler.registerAbility("Void Lantern", "Dark Beacon", VoidLanternAbility::executeDarkBeacon);
        AbilityCastHandler.registerAbility("Void Lantern", "Dimensional Tear", VoidLanternAbility::executeDimensionalTear);
    }

    private static void executeDarkBeacon(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        float damage = (float) stats[0];
        double radius = stats[1];
        ServerLevel level = (ServerLevel) player.level();

        // Target position 8 blocks in front of player
        Vec3 look = player.getLookAngle();
        Vec3 eye = player.getEyePosition();
        double tx = eye.x + look.x * 8.0;
        double ty = eye.y + look.y * 8.0;
        double tz = eye.z + look.z * 8.0;

        AABB area = new AABB(tx - radius, ty - radius, tz - radius,
                tx + radius, ty + radius, tz + radius);
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, area,
                e -> e != player && e.isAlive());

        for (LivingEntity entity : entities) {
            double dist = entity.position().distanceTo(new Vec3(tx, ty, tz));
            if (dist <= radius) {
                entity.hurt(level.damageSources().magic(), damage);
                // Pull toward center
                double dx = tx - entity.getX();
                double dy = ty - entity.getY();
                double dz = tz - entity.getZ();
                double d = Math.sqrt(dx * dx + dy * dy + dz * dz);
                if (d > 0.5) {
                    entity.setDeltaMovement(entity.getDeltaMovement().add(
                            dx / d * 0.6, dy / d * 0.3, dz / d * 0.6));
                    entity.hurtMarked = true;
                }
            }
        }

        WeaponEffects.converge(level, (ParticleOptions) ParticleTypes.REVERSE_PORTAL,
                tx, ty, tz, radius, 20, 3);
        WeaponEffects.converge(level, (ParticleOptions) ParticleTypes.DRAGON_BREATH,
                tx, ty, tz, radius * 0.6, 12, 2);
        level.playSound(null, tx, ty, tz,
                SoundEvents.WITHER_SPAWN, SoundSource.PLAYERS, 1.0f, 1.5f);
    }

    private static void executeDimensionalTear(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        float damage = (float) stats[0];
        double radius = stats[1];
        ServerLevel level = (ServerLevel) player.level();

        // Target position at look direction
        Vec3 look = player.getLookAngle();
        Vec3 eye = player.getEyePosition();
        double tx = eye.x + look.x * 8.0;
        double ty = eye.y + look.y * 8.0;
        double tz = eye.z + look.z * 8.0;

        AABB area = new AABB(tx - radius, ty - radius, tz - radius,
                tx + radius, ty + radius, tz + radius);
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, area,
                e -> e != player && e.isAlive());

        for (LivingEntity entity : entities) {
            double dist = entity.position().distanceTo(new Vec3(tx, ty, tz));
            if (dist <= radius) {
                entity.hurt(level.damageSources().magic(), damage);
            }
        }

        WeaponEffects.ring(level, (ParticleOptions) ParticleTypes.REVERSE_PORTAL,
                tx, ty - 0.5, tz, radius, 20, 3, 0.05);
        WeaponEffects.ring(level, (ParticleOptions) ParticleTypes.REVERSE_PORTAL,
                tx, ty + 0.5, tz, radius * 0.8, 16, 2, 0.05);
        WeaponEffects.ring(level, (ParticleOptions) ParticleTypes.REVERSE_PORTAL,
                tx, ty + 1.5, tz, radius * 0.5, 12, 2, 0.05);
        WeaponEffects.ring(level, (ParticleOptions) ParticleTypes.DRAGON_BREATH,
                tx, ty, tz, radius * 0.6, 12, 2, 0.03);
        level.playSound(null, tx, ty, tz,
                SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.5f, 0.5f);
    }
}
