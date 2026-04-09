package com.ultra.megamod.feature.citizen.visitor;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.toggles.FeatureToggleManager;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Event handler for the visitor/tavern recruitment system.
 * <p>
 * Ticks all VisitorManagers to handle visitor expiry (leaving after 3 in-game days).
 * Visitor spawning is triggered by tavern building placement/upgrade via
 * {@link VisitorManager#spawnVisitors(ServerLevel, net.minecraft.core.BlockPos, int)},
 * not by this tick handler.
 */
@EventBusSubscriber(modid = MegaMod.MODID)
public class VisitorEvents {

    // Tick visitors every 200 ticks (10 seconds) to avoid per-tick overhead
    private static final int TICK_INTERVAL = 200;

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        ServerLevel level = event.getServer().overworld();
        if (!FeatureToggleManager.get(level).isEnabled("citizens")) return;

        long tick = level.getServer().getTickCount();

        // Tick all visitor managers for expiry checks
        if (tick % TICK_INTERVAL == 0) {
            for (VisitorManager mgr : VisitorManager.allInstances()) {
                mgr.tickVisitors(level);
            }
        }
    }

    /**
     * Called when a tavern building is upgraded or first built.
     * Triggers visitor spawning for the faction that owns the tavern.
     *
     * @param level       the server level
     * @param factionId   the faction that owns the tavern
     * @param tavernPos   the tavern hut block position
     * @param tavernLevel the new tavern building level (1-5)
     */
    public static void onTavernUpgraded(ServerLevel level, String factionId,
                                        net.minecraft.core.BlockPos tavernPos, int tavernLevel) {
        if (factionId == null || factionId.isEmpty()) return;
        VisitorManager mgr = VisitorManager.get(level, factionId);
        mgr.spawnVisitors(level, tavernPos, tavernLevel);
    }
}
