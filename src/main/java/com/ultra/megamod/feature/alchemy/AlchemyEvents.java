package com.ultra.megamod.feature.alchemy;

import com.ultra.megamod.MegaMod;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

/**
 * Handles AlchemyManager lifecycle: load on world load, save on server stop.
 */
@EventBusSubscriber(modid = MegaMod.MODID)
public class AlchemyEvents {

    @SubscribeEvent
    public static void onWorldLoad(LevelEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel serverLevel && serverLevel.dimension() == net.minecraft.world.level.Level.OVERWORLD) {
            AlchemyManager.get(serverLevel); // Trigger load
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        ServerLevel overworld = event.getServer().overworld();
        if (overworld != null) {
            AlchemyManager mgr = AlchemyManager.get(overworld);
            mgr.saveToDisk(overworld);
        }
        AlchemyManager.reset();
    }
}
