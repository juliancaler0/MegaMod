package com.ultra.megamod.feature.economy;

import com.ultra.megamod.MegaMod;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Server-side event handler for economy analytics.
 * Takes periodic snapshots of economy state.
 */
@EventBusSubscriber(modid = MegaMod.MODID)
public class EconomyAnalyticsEvents {

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        ServerLevel overworld = event.getServer().overworld();
        if (overworld == null) return;

        EconomyManager eco = EconomyManager.get(overworld);
        EconomyAnalytics analytics = EconomyAnalytics.get(overworld);
        analytics.onServerTick(overworld, eco);
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        ServerLevel overworld = event.getServer().overworld();
        if (overworld != null) {
            // Initialize analytics on server start
            EconomyAnalytics.get(overworld);
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        ServerLevel overworld = event.getServer().overworld();
        if (overworld != null) {
            EconomyAnalytics analytics = EconomyAnalytics.getIfLoaded();
            if (analytics != null) {
                analytics.saveToDisk(overworld);
            }
        }
        EconomyAnalytics.reset();
    }
}
