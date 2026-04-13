package com.ultra.megamod.feature.computer.screen.map;

import com.ultra.megamod.MegaMod;
import net.minecraft.core.BlockPos;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;

/**
 * Listens for block changes and marks the affected map tiles as dirty.
 *
 * BlockEvent.BreakEvent / EntityPlaceEvent only fire server-side, so this
 * subscriber only catches changes when the integrated server is in-process
 * (singleplayer / LAN host). On dedicated multiplayer the player's tile still
 * refreshes via MapChunkTileManager.updateCurrentTile (per-tick dirty marking).
 */
@EventBusSubscriber(modid = MegaMod.MODID, value = Dist.CLIENT)
public class MapBlockChangeListener {

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        markDirty(event.getPos());
    }

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        markDirty(event.getPos());
    }

    private static void markDirty(BlockPos pos) {
        try {
            MapChunkTileManager.getInstance().markTileDirty(pos.getX(), pos.getZ());
        } catch (Exception ignored) {
            // Manager may not be initialized yet
        }
    }
}
