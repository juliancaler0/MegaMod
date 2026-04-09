package com.ultra.megamod.feature.corruption.client;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.corruption.network.CorruptionZoneSyncPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.DustParticleOptions;
// ParticleTypes not used — all particles use DustParticleOptions
// PowerParticleOption removed — using DustParticleOptions for all particles
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.List;

/**
 * Client-side particle renderer for corruption zone boundaries.
 * Spawns green and black dust particles along the edge of each zone circle,
 * with occasional dragon breath particles for extra visual flair.
 * Also spawns sparse interior particles for atmosphere.
 */
@EventBusSubscriber(modid = MegaMod.MODID, value = Dist.CLIENT)
public class CorruptionZoneBoundaryRenderer {

    // Maximum render distance for boundary particles from the player
    private static final double PARTICLE_RENDER_DIST_SQ = 64.0 * 64.0;
    // Maximum distance from player to consider a zone for rendering at all
    private static final double ZONE_RENDER_RANGE = 200.0;
    // Maximum render distance for interior particles
    private static final double INTERIOR_RENDER_DIST_SQ = 48.0 * 48.0;

    // Particle colors as packed ARGB ints for DustParticleOptions(int, float)
    private static final int GREEN_COLOR = 0xFF199A0D;    // dark sickly green
    private static final int BLACK_COLOR = 0xFF0D0514;    // near-black with purple
    private static final int GLOW_COLOR = 0xFF33CC1A;     // bright green glow

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        // Only spawn particles every 2 ticks for performance
        if (mc.player.tickCount % 2 != 0) return;

        // Get corruption zone data from client tracker
        List<CorruptionZoneSyncPayload.ZoneEntry> zones = CorruptionClientTracker.getActiveZones();
        if (zones == null || zones.isEmpty()) return;

        double playerX = mc.player.getX();
        double playerZ = mc.player.getZ();

        for (CorruptionZoneSyncPayload.ZoneEntry zone : zones) {
            // Only render zones within range of player
            double dx = zone.centerX() - playerX;
            double dz = zone.centerZ() - playerZ;
            double dist = Math.sqrt(dx * dx + dz * dz);
            if (dist > zone.radius() + ZONE_RENDER_RANGE) continue;

            spawnBoundaryParticles(mc, zone, playerX, playerZ);
        }
    }

    private static void spawnBoundaryParticles(Minecraft mc, CorruptionZoneSyncPayload.ZoneEntry zone,
                                                double playerX, double playerZ) {
        RandomSource random = mc.level.getRandom();

        // Scale particle count with zone size, cap at 30 boundary particles per zone per tick
        int particleCount = Math.min(zone.radius() / 2, 30);

        for (int i = 0; i < particleCount; i++) {
            // Random angle around the circle boundary
            double angle = random.nextDouble() * Math.PI * 2;
            // Slight radius variation for natural look (+/- 3 blocks from boundary)
            double r = zone.radius() + (random.nextDouble() * 6.0 - 3.0);

            double x = zone.centerX() + 0.5 + Math.cos(angle) * r;
            double z = zone.centerZ() + 0.5 + Math.sin(angle) * r;

            // Only spawn if near player
            double dx = x - playerX;
            double dz = z - playerZ;
            if (dx * dx + dz * dz > PARTICLE_RENDER_DIST_SQ) continue;

            double y = mc.level.getHeight(Heightmap.Types.MOTION_BLOCKING, (int) x, (int) z)
                    + random.nextDouble() * 3.0;

            // Alternate between green and black particles
            if (random.nextBoolean()) {
                // Dark sickly green dust (like corrupted dragon breath)
                DustParticleOptions green = new DustParticleOptions(
                        GREEN_COLOR,
                        1.5f + random.nextFloat() * 0.5f  // size 1.5-2.0
                );
                mc.level.addParticle(green, x, y, z,
                        (random.nextDouble() - 0.5) * 0.02,   // slow horizontal drift
                        random.nextDouble() * 0.05 + 0.02,    // slow upward drift
                        (random.nextDouble() - 0.5) * 0.02);
            } else {
                // Near-black dust with slight purple tint
                DustParticleOptions black = new DustParticleOptions(
                        BLACK_COLOR,
                        1.2f + random.nextFloat() * 0.8f  // size 1.2-2.0
                );
                mc.level.addParticle(black, x, y, z,
                        (random.nextDouble() - 0.5) * 0.03,
                        random.nextDouble() * 0.04,
                        (random.nextDouble() - 0.5) * 0.03);
            }

            // 15% chance for extra green dust particle (larger, slower) for dragon-breath-like cloud
            if (random.nextFloat() < 0.15f) {
                DustParticleOptions glow = new DustParticleOptions(GLOW_COLOR, 2.5f);
                mc.level.addParticle(glow, x, y + 0.5, z,
                        (random.nextDouble() - 0.5) * 0.01,
                        random.nextDouble() * 0.02,
                        (random.nextDouble() - 0.5) * 0.01);
            }
        }

        // Sparse interior particles for atmosphere
        int interiorCount = Math.min(zone.radius() / 8, 10);
        for (int i = 0; i < interiorCount; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double r = random.nextDouble() * zone.radius();
            double x = zone.centerX() + 0.5 + Math.cos(angle) * r;
            double z = zone.centerZ() + 0.5 + Math.sin(angle) * r;

            double dx = x - playerX;
            double dz = z - playerZ;
            if (dx * dx + dz * dz > INTERIOR_RENDER_DIST_SQ) continue;

            double y = mc.level.getHeight(Heightmap.Types.MOTION_BLOCKING, (int) x, (int) z)
                    + random.nextDouble() * 2.0;

            // Slightly varying green for interior ambiance
            // Slightly varying green for interior
            int gr = 76 + random.nextInt(51);
            int rd = 12 + random.nextInt(26);
            int interiorArgb = 0xFF000000 | (rd << 16) | (gr << 8) | 5;
            DustParticleOptions interior = new DustParticleOptions(
                    interiorArgb,
                    0.8f + random.nextFloat() * 0.4f
            );
            mc.level.addParticle(interior, x, y, z, 0, 0.01, 0);
        }
    }
}
