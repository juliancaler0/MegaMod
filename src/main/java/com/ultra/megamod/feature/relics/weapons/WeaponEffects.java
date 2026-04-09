package com.ultra.megamod.feature.relics.weapons;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

/**
 * Reusable particle effect patterns for weapon abilities.
 */
public final class WeaponEffects {
    private WeaponEffects() {}

    /**
     * Spawn particles in a horizontal ring.
     */
    public static void ring(ServerLevel level, ParticleOptions particle, double cx, double cy, double cz,
                            double radius, int points, int countPerPoint, double spread) {
        double step = 2.0 * Math.PI / points;
        for (int i = 0; i < points; i++) {
            double angle = step * i;
            double x = cx + radius * Math.cos(angle);
            double z = cz + radius * Math.sin(angle);
            level.sendParticles(particle, x, cy, z, countPerPoint, spread, spread * 0.5, spread, 0.0);
        }
    }

    /**
     * Spawn particles along a line between two points.
     */
    public static void line(ServerLevel level, ParticleOptions particle, Vec3 from, Vec3 to,
                            int points, int countPerPoint, double spread) {
        for (int i = 0; i <= points; i++) {
            double t = (double) i / points;
            double x = from.x + (to.x - from.x) * t;
            double y = from.y + (to.y - from.y) * t;
            double z = from.z + (to.z - from.z) * t;
            level.sendParticles(particle, x, y, z, countPerPoint, spread, spread, spread, 0.0);
        }
    }

    /**
     * Spawn particles in a vertical column.
     */
    public static void column(ServerLevel level, ParticleOptions particle, double cx, double baseY, double cz,
                              double height, int points, int countPerPoint, double spread) {
        double step = height / points;
        for (int i = 0; i <= points; i++) {
            double y = baseY + step * i;
            level.sendParticles(particle, cx, y, cz, countPerPoint, spread, 0.05, spread, 0.0);
        }
    }

    /**
     * Spawn particles in a spiral rising upward.
     */
    public static void spiral(ServerLevel level, ParticleOptions particle, double cx, double baseY, double cz,
                              double radius, double height, int points, int countPerPoint) {
        double angleStep = 4.0 * Math.PI / points; // 2 full rotations
        double yStep = height / points;
        for (int i = 0; i < points; i++) {
            double angle = angleStep * i;
            double r = radius * (1.0 - (double) i / points * 0.3); // slight taper
            double x = cx + r * Math.cos(angle);
            double y = baseY + yStep * i;
            double z = cz + r * Math.sin(angle);
            level.sendParticles(particle, x, y, z, countPerPoint, 0.02, 0.02, 0.02, 0.0);
        }
    }

    /**
     * Spawn particles in a sphere burst.
     */
    public static void sphere(ServerLevel level, ParticleOptions particle, double cx, double cy, double cz,
                              double radius, int count) {
        level.sendParticles(particle, cx, cy, cz, count, radius, radius, radius, 0.02);
    }

    /**
     * Spawn particles along an arc in front of a look direction.
     * sweepAngle in radians (e.g. PI/2 for 90-degree arc).
     */
    public static void arc(ServerLevel level, ParticleOptions particle, double cx, double cy, double cz,
                           double lookX, double lookZ, double radius, double sweepAngle,
                           int points, int countPerPoint) {
        double baseAngle = Math.atan2(lookZ, lookX);
        double halfSweep = sweepAngle / 2.0;
        double step = sweepAngle / points;
        for (int i = 0; i <= points; i++) {
            double angle = baseAngle - halfSweep + step * i;
            double x = cx + radius * Math.cos(angle);
            double z = cz + radius * Math.sin(angle);
            level.sendParticles(particle, x, cy, z, countPerPoint, 0.05, 0.1, 0.05, 0.0);
        }
    }

    /**
     * Spawn a converging particle burst toward a target point (like something closing in).
     */
    public static void converge(ServerLevel level, ParticleOptions particle, double tx, double ty, double tz,
                                double radius, int points, int countPerPoint) {
        double step = 2.0 * Math.PI / points;
        for (int i = 0; i < points; i++) {
            double angle = step * i;
            double x = tx + radius * Math.cos(angle);
            double z = tz + radius * Math.sin(angle);
            // Speed toward target
            double dx = (tx - x) * 0.15;
            double dz = (tz - z) * 0.15;
            level.sendParticles(particle, x, ty, z, countPerPoint, dx, 0.05, dz, 0.1);
        }
    }

    /**
     * Spawn an expanding shockwave ring on the ground (multiple radii for thickness).
     */
    public static void shockwave(ServerLevel level, ParticleOptions particle, double cx, double cy, double cz,
                                 double maxRadius, int rings, int pointsPerRing, int countPerPoint) {
        double radiusStep = maxRadius / rings;
        for (int r = 1; r <= rings; r++) {
            ring(level, particle, cx, cy, cz, radiusStep * r, pointsPerRing, countPerPoint, 0.02);
        }
    }
}
