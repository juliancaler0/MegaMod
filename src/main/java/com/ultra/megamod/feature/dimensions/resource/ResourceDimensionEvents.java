package com.ultra.megamod.feature.dimensions.resource;

import com.ultra.megamod.MegaMod;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = "megamod")
public class ResourceDimensionEvents {

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        ServerLevel overworld = event.getServer().overworld();
        long gameTime = overworld.getGameTime();
        // Check every 60 seconds (1200 ticks)
        if (gameTime % 1200L != 0L) {
            return;
        }
        ResourceDimensionManager manager = ResourceDimensionManager.get(overworld);
        manager.checkAndReset(overworld);
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        ServerLevel overworld = event.getServer().overworld();
        ResourceDimensionManager manager = ResourceDimensionManager.get(overworld);
        manager.saveToDisk(overworld);
        ResourceDimensionManager.reset();
    }
}
