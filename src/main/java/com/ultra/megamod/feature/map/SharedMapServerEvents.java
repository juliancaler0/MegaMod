package com.ultra.megamod.feature.map;

import com.ultra.megamod.MegaMod;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = MegaMod.MODID)
public class SharedMapServerEvents {

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        SharedMapManager mgr = SharedMapManager.getInstance();
        if (mgr.isInitialized()) {
            mgr.tick(event.getServer());
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) {
            SharedMapManager mgr = SharedMapManager.getInstance();
            mgr.onPlayerDisconnect(sp.getUUID());
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        SharedMapManager.reset();
    }
}
