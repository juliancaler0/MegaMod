package com.ultra.megamod.feature.computer.screen.map;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.map.SharedMapTileReceiver;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.level.LevelEvent;

/**
 * Pre-renders map chunks in the background as the player explores,
 * so the map screen opens instantly with cached tile data.
 * Runs every 2 seconds on the client tick, submitting one tile at a time.
 * Also handles world disconnect cleanup.
 */
@EventBusSubscriber(modid = MegaMod.MODID, value = Dist.CLIENT)
public class MapBackgroundRenderer {
    private static int tickCounter = 0;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        tickCounter++;
        if (tickCounter % 40 != 0) return; // Every 2 seconds

        MapChunkTileManager mgr = MapChunkTileManager.getInstance();
        mgr.initializeIfNeeded(mc.level);
        mgr.backgroundTick();
        SharedMapTileReceiver.flushPendingRequests(mgr.isCaveView());
    }

    @SubscribeEvent
    public static void onWorldUnload(LevelEvent.Unload event) {
        if (event.getLevel().isClientSide()) {
            MapChunkTileManager.getInstance().close();
        }
    }
}
