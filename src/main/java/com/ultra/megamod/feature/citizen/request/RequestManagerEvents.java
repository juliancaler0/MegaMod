package com.ultra.megamod.feature.citizen.request;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.toggles.FeatureToggleManager;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Event handler for the colony request system.
 * Manages lifecycle hooks: server start, tick, and stop.
 */
@EventBusSubscriber(modid = MegaMod.MODID)
public class RequestManagerEvents {

    private static long lastSaveTick = 0;
    private static final long SAVE_INTERVAL = 6000; // auto-save every 5 minutes

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        ServerLevel level = event.getServer().overworld();
        RequestManager.get(level);
        MegaMod.LOGGER.info("Request system initialized");
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        ServerLevel level = event.getServer().overworld();
        if (!FeatureToggleManager.get(level).isEnabled("citizens")) return;

        long tick = level.getServer().getTickCount();
        RequestManager manager = RequestManager.get(level);
        manager.tick(level);

        // Periodic save
        if (tick - lastSaveTick >= SAVE_INTERVAL) {
            manager.saveToDisk(level);
            lastSaveTick = tick;
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        ServerLevel level = event.getServer().overworld();
        RequestManager.get(level).saveToDisk(level);
        RequestManager.reset();
        lastSaveTick = 0;
        MegaMod.LOGGER.info("Request system saved and shut down");
    }
}
