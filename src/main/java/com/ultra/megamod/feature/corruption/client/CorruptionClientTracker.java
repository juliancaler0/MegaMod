package com.ultra.megamod.feature.corruption.client;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.corruption.network.CorruptionSyncPayload;
import com.ultra.megamod.feature.corruption.network.CorruptionZoneSyncPayload;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Client-side tracker that detects corruption zone presence based on
 * the actionbar message and debuff effects applied by the server.
 *
 * The server sends the action bar message "You feel the corruption seeping into your bones..."
 * and applies specific debuff combos. We detect these to determine approximate strength.
 *
 * Alternatively, a network packet could be used, but this approach keeps things simple
 * and avoids adding a new payload type just for the overlay.
 */
@EventBusSubscriber(modid = MegaMod.MODID, value = Dist.CLIENT)
public class CorruptionClientTracker {

    // Current corruption strength detected on the client (0 = not in corruption)
    private static int currentStrength = 0;

    // How long the corruption indicator persists after last detection (ticks)
    private static final int PERSIST_TICKS = 80; // 4 seconds
    private static int ticksSinceDetected = 0;

    // The server sets this via the CorruptionSyncPayload
    // -1 means no new data, >= 0 means server reported strength
    private static volatile int serverReportedStrength = -1;

    // Zone boundary data synced from server via CorruptionZoneSyncPayload
    private static volatile List<CorruptionZoneSyncPayload.ZoneEntry> activeZones = Collections.emptyList();

    public static int getCurrentStrength() {
        return currentStrength;
    }

    /**
     * Called from CorruptionSyncPayload handler when the server sends corruption strength.
     */
    public static void setStrength(int strength) {
        serverReportedStrength = strength;
    }

    /**
     * Called from CorruptionZoneSyncPayload handler when the server sends zone boundaries.
     */
    public static void setZones(List<CorruptionZoneSyncPayload.ZoneEntry> zones) {
        activeZones = zones != null ? new ArrayList<>(zones) : Collections.emptyList();
    }

    /**
     * Returns the list of active corruption zones synced from the server.
     * Used by CorruptionZoneBoundaryRenderer for particle effects.
     */
    public static List<CorruptionZoneSyncPayload.ZoneEntry> getActiveZones() {
        return activeZones;
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            currentStrength = 0;
            activeZones = Collections.emptyList();
            return;
        }

        // Primary: Use server-reported strength from payload static field
        int reported = CorruptionSyncPayload.clientCorruptionStrength;
        if (reported >= 0) {
            CorruptionSyncPayload.clientCorruptionStrength = -1; // consume
            if (reported > 0) {
                currentStrength = reported;
                ticksSinceDetected = 0;
            } else {
                // Server reports 0 — player left corrupted zone
                currentStrength = 0;
                ticksSinceDetected = PERSIST_TICKS;
            }
            return;
        }

        // Fallback: fade out naturally if no server updates
        ticksSinceDetected++;
        if (ticksSinceDetected >= PERSIST_TICKS) {
            currentStrength = 0;
        }
    }
}
