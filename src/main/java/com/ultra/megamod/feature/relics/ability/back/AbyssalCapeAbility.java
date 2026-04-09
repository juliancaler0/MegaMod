package com.ultra.megamod.feature.relics.ability.back;

import com.ultra.megamod.feature.relics.ability.AbilityCastHandler;
import com.ultra.megamod.feature.relics.data.RelicAbility;
import com.ultra.megamod.feature.relics.data.RelicStat;
import com.ultra.megamod.feature.relics.weapons.WeaponEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import java.util.List;

public class AbyssalCapeAbility {
    public static final List<RelicAbility> ABILITIES = List.of(
            new RelicAbility("Void Walker", "Reduces fall damage passively", 1,
                    RelicAbility.CastType.PASSIVE,
                    List.of(new RelicStat("reduction", 40.0, 70.0, RelicStat.ScaleType.ADD, 5.0))),
            new RelicAbility("Blink", "Short-range teleport in look direction", 3,
                    RelicAbility.CastType.INSTANTANEOUS,
                    List.of(new RelicStat("distance", 5.0, 9.0, RelicStat.ScaleType.ADD, 0.6))),
            new RelicAbility("Void Rift", "Tear a rift that damages and pulls enemies toward you", 7,
                    RelicAbility.CastType.INSTANTANEOUS,
                    List.of(new RelicStat("damage", 5.0, 10.0, RelicStat.ScaleType.MULTIPLY_BASE, 0.1)))
    );

    public static void register() {
        AbilityCastHandler.registerAbility("Abyssal Cape", "Void Walker", AbyssalCapeAbility::executeVoidWalker);
        AbilityCastHandler.registerAbility("Abyssal Cape", "Blink", AbyssalCapeAbility::executeBlink);
        AbilityCastHandler.registerAbility("Abyssal Cape", "Void Rift", AbyssalCapeAbility::executeVoidRift);
    }

    private static void executeVoidWalker(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        if (player.tickCount % 20 != 0) return;
        double reductionPercent = stats[0];
        player.fallDistance *= (float) (1.0 - reductionPercent / 100.0);

        if (player.tickCount % 60 == 0) {
            ServerLevel level = (ServerLevel) player.level();
            level.sendParticles((ParticleOptions) ParticleTypes.REVERSE_PORTAL,
                    player.getX(), player.getY() + 0.5, player.getZ(), 3, 0.2, 0.3, 0.2, 0.01);
        }
    }

    private static void executeBlink(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        double range = stats[0];
        ServerLevel level = (ServerLevel) player.level();
        Vec3 look = player.getLookAngle().normalize();
        Vec3 start = player.position();
        Vec3 targetPos = start;

        for (double d = 1.0; d <= range; d += 0.5) {
            Vec3 candidate = start.add(look.scale(d));
            BlockPos feetPos = BlockPos.containing(candidate.x, candidate.y, candidate.z);
            BlockPos headPos = feetPos.above();
            boolean feetClear = level.getBlockState(feetPos).getCollisionShape((BlockGetter) level, feetPos).isEmpty();
            boolean headClear = level.getBlockState(headPos).getCollisionShape((BlockGetter) level, headPos).isEmpty();
            if (!feetClear || !headClear) break;
            targetPos = candidate;
        }

        if (targetPos.distanceTo(start) > 0.5) {
            // Particles at origin
            level.sendParticles((ParticleOptions) ParticleTypes.REVERSE_PORTAL,
                    start.x, start.y + 1.0, start.z, 20, 0.3, 0.5, 0.3, 0.05);

            player.teleportTo(targetPos.x, targetPos.y, targetPos.z);
            player.fallDistance = 0.0f;

            // Particles at destination
            level.sendParticles((ParticleOptions) ParticleTypes.SOUL,
                    targetPos.x, targetPos.y + 1.0, targetPos.z, 15, 0.3, 0.5, 0.3, 0.02);

            level.playSound(null, targetPos.x, targetPos.y, targetPos.z,
                    SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.8f, 0.6f);
        }
    }

    private static void executeVoidRift(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        double damage = stats[0];
        ServerLevel level = (ServerLevel) player.level();
        Vec3 look = player.getLookAngle().normalize();
        Vec3 start = player.position().add(0, player.getBbHeight() / 2.0, 0);
        Vec3 end = start.add(look.scale(10.0));

        // Line particle effect
        WeaponEffects.line(level, (ParticleOptions) ParticleTypes.DRAGON_BREATH, start, end, 20, 3, 0.1);
        WeaponEffects.line(level, (ParticleOptions) ParticleTypes.REVERSE_PORTAL, start, end, 15, 2, 0.15);

        // Get mobs along the line (use a wide AABB covering the line)
        double minX = Math.min(start.x, end.x) - 2.0;
        double minY = Math.min(start.y, end.y) - 2.0;
        double minZ = Math.min(start.z, end.z) - 2.0;
        double maxX = Math.max(start.x, end.x) + 2.0;
        double maxY = Math.max(start.y, end.y) + 2.0;
        double maxZ = Math.max(start.z, end.z) + 2.0;
        AABB area = new AABB(minX, minY, minZ, maxX, maxY, maxZ);
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, area, e -> e != player && e.isAlive());

        // Midpoint for converge effect
        Vec3 midpoint = start.add(end).scale(0.5);

        for (LivingEntity entity : entities) {
            // Check if entity is close to the line
            Vec3 entityPos = entity.position().add(0, entity.getBbHeight() / 2.0, 0);
            Vec3 toEntity = entityPos.subtract(start);
            Vec3 lineDir = end.subtract(start).normalize();
            double projLength = toEntity.dot(lineDir);
            if (projLength < 0 || projLength > start.distanceTo(end)) continue;
            Vec3 closestOnLine = start.add(lineDir.scale(projLength));
            double distToLine = entityPos.distanceTo(closestOnLine);
            if (distToLine > 2.0) continue;

            entity.hurt(level.damageSources().magic(), (float) damage);

            // Pull toward player
            Vec3 pullDir = player.position().subtract(entity.position()).normalize().scale(0.6);
            entity.setDeltaMovement(entity.getDeltaMovement().add(pullDir));
            entity.hurtMarked = true;
        }

        WeaponEffects.converge(level, (ParticleOptions) ParticleTypes.REVERSE_PORTAL,
                midpoint.x, midpoint.y, midpoint.z, 3.0, 12, 2);

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.WITHER_HURT, SoundSource.PLAYERS, 0.8f, 0.5f);
    }
}
