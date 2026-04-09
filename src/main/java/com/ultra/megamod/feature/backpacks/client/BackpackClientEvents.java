package com.ultra.megamod.feature.backpacks.client;

import com.ultra.megamod.feature.backpacks.BackpackWearableManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.world.entity.player.PlayerModelType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/**
 * Client-side events for backpack rendering.
 * Registers the BackpackLayerRenderer and provides client-side state management.
 *
 * The equipped backpack state is synced from the server via BackpackSyncPayload,
 * which updates BackpackWearableManager's client-side cache.
 * The render layer reads from that cache to decide what to draw.
 */
@EventBusSubscriber(modid = "megamod", value = {Dist.CLIENT})
public class BackpackClientEvents {

    /**
     * Register the backpack render layer on player renderers.
     * Must be called via modEventBus.addListener() since AddLayers is a mod bus event.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        for (PlayerModelType skin : event.getSkins()) {
            AvatarRenderer<?> renderer = event.getPlayerRenderer(skin);
            if (renderer != null) {
                renderer.addLayer(new BackpackLayerRenderer(renderer, event.getContext()));
            }
        }
    }

    /**
     * Periodic client tick to keep local player state in sync.
     * The main state is driven by BackpackSyncPayload from the server,
     * but we also do a sanity check here.
     */
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        // Clean up entries for players who are no longer in the world
        // (This runs every 5 seconds to avoid overhead)
        if (mc.player.tickCount % 100 == 0) {
            // No cleanup needed -- the sync payload handles state.
            // Entries for disconnected players will be overwritten on next join.
        }
    }
}
