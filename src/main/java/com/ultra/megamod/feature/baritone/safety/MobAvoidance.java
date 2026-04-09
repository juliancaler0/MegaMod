package com.ultra.megamod.feature.baritone.safety;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

/**
 * Scans for hostile mobs near a path and provides avoidance data.
 * Used both for pathfinding cost penalties and for runtime safety checks.
 */
public class MobAvoidance {
    private final List<MobInfo> nearbyMobs;

    public record MobInfo(int x, int y, int z, String type, double health) {}

    private MobAvoidance(List<MobInfo> mobs) {
        this.nearbyMobs = mobs;
    }

    /**
     * Scan for hostile mobs within radius of the given position.
     */
    public static MobAvoidance scan(ServerLevel level, BlockPos center, int radius) {
        List<MobInfo> mobs = new ArrayList<>();
        AABB scanBox = new AABB(center).inflate(radius);

        for (Entity entity : level.getEntities((Entity) null, scanBox, e -> e instanceof Monster && e.isAlive())) {
            BlockPos pos = entity.blockPosition();
            mobs.add(new MobInfo(
                pos.getX(), pos.getY(), pos.getZ(),
                entity.getType().toShortString(),
                entity instanceof net.minecraft.world.entity.LivingEntity le ? le.getHealth() : 0
            ));
        }

        return new MobAvoidance(mobs);
    }

    /**
     * Check if a position is dangerously close to a hostile mob.
     */
    public boolean isDangerous(int x, int y, int z, int safeRadius) {
        int safeRadiusSq = safeRadius * safeRadius;
        for (MobInfo mob : nearbyMobs) {
            int dx = x - mob.x;
            int dy = y - mob.y;
            int dz = z - mob.z;
            if (dx * dx + dy * dy + dz * dz < safeRadiusSq) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get the cost penalty for being near mobs at the given position.
     */
    public double getCostPenalty(int x, int y, int z, int avoidRadius) {
        if (nearbyMobs.isEmpty()) return 0;
        double penalty = 0;
        int avoidRadiusSq = avoidRadius * avoidRadius;
        for (MobInfo mob : nearbyMobs) {
            int dx = x - mob.x;
            int dy = y - mob.y;
            int dz = z - mob.z;
            int distSq = dx * dx + dy * dy + dz * dz;
            if (distSq < avoidRadiusSq) {
                double factor = 1.0 - ((double) distSq / avoidRadiusSq);
                penalty += 15.0 * factor;
            }
        }
        return penalty;
    }

    public List<MobInfo> getMobs() { return nearbyMobs; }
    public int count() { return nearbyMobs.size(); }
    public boolean isEmpty() { return nearbyMobs.isEmpty(); }
}
