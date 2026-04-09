package com.ultra.megamod.feature.baritone.pathfinding;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

/**
 * Mob/entity avoidance cost modifier for pathfinding.
 * Scans for hostile mobs near the player and adds cost penalties
 * to path nodes near them.
 */
public class Avoidance {
    private final List<AvoidanceZone> zones;
    private static final double AVOIDANCE_COST = 20.0;
    private static final int DEFAULT_RADIUS = 8;

    public record AvoidanceZone(int x, int y, int z, int radiusSq, double costPenalty) {}

    private Avoidance(List<AvoidanceZone> zones) {
        this.zones = zones;
    }

    /**
     * Create avoidance zones from hostile mobs near the given position.
     */
    public static Avoidance create(ServerLevel level, BlockPos center, int scanRadius) {
        List<AvoidanceZone> zones = new ArrayList<>();
        AABB scanBox = new AABB(center).inflate(scanRadius);

        for (Entity entity : level.getEntities((Entity) null, scanBox, e -> e instanceof Monster && e.isAlive())) {
            BlockPos pos = entity.blockPosition();
            zones.add(new AvoidanceZone(
                pos.getX(), pos.getY(), pos.getZ(),
                DEFAULT_RADIUS * DEFAULT_RADIUS,
                AVOIDANCE_COST
            ));
        }

        return new Avoidance(zones);
    }

    /**
     * Create an empty avoidance (no penalties).
     */
    public static Avoidance empty() {
        return new Avoidance(List.of());
    }

    /**
     * Get the additional cost penalty for a node at the given position.
     */
    public double costAt(int x, int y, int z) {
        if (zones.isEmpty()) return 0;
        double totalPenalty = 0;
        for (AvoidanceZone zone : zones) {
            int dx = x - zone.x;
            int dy = y - zone.y;
            int dz = z - zone.z;
            int distSq = dx * dx + dy * dy + dz * dz;
            if (distSq < zone.radiusSq) {
                // Penalty scales inversely with distance
                double factor = 1.0 - ((double) distSq / zone.radiusSq);
                totalPenalty += zone.costPenalty * factor;
            }
        }
        return totalPenalty;
    }

    public boolean isEmpty() {
        return zones.isEmpty();
    }

    public int zoneCount() {
        return zones.size();
    }
}
