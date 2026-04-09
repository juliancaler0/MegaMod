package com.ultra.megamod.feature.combat.spell.client;

import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Client-side state tracker for active beam spell visuals.
 * Beams are added when a BeamSyncPayload arrives and fade out over their duration.
 */
public class BeamRenderState {

    private static final List<ActiveBeam> activeBeams = new CopyOnWriteArrayList<>();

    public record ActiveBeam(
            Vec3 start,
            Vec3 end,
            int schoolOrdinal,
            long startTimeMs,
            int durationMs
    ) {
        public float progress() {
            long elapsed = System.currentTimeMillis() - startTimeMs;
            return Math.min((float) elapsed / durationMs, 1.0f);
        }

        public boolean isExpired() {
            return System.currentTimeMillis() - startTimeMs > durationMs;
        }

        /** Returns opacity (1.0 at start, fades to 0.0 at end). */
        public float opacity() {
            float p = progress();
            // Full opacity for first 60%, then fade out
            if (p < 0.6f) return 1.0f;
            return 1.0f - (p - 0.6f) / 0.4f;
        }

        /** Returns the beam color as ARGB based on spell school. */
        public int getColor() {
            return switch (schoolOrdinal) {
                case 1 -> 0xFFFF66FF; // ARCANE - purple
                case 2 -> 0xFFFF3300; // FIRE - orange-red
                case 3 -> 0xFFCCFFFF; // FROST - cyan
                case 4 -> 0xFF66FF66; // HEALING - green
                case 5 -> 0xFFFFFF99; // LIGHTNING - yellow
                case 6 -> 0xFF2DD4DA; // SOUL - teal
                default -> 0xFFFFFFFF; // White fallback
            };
        }
    }

    public static void addBeam(Vec3 start, Vec3 end, int schoolOrdinal, int durationMs) {
        activeBeams.add(new ActiveBeam(start, end, schoolOrdinal, System.currentTimeMillis(), durationMs));
    }

    public static List<ActiveBeam> getActiveBeams() {
        return activeBeams;
    }

    public static void tick() {
        activeBeams.removeIf(ActiveBeam::isExpired);
    }

    public static void clearAll() {
        activeBeams.clear();
    }
}
