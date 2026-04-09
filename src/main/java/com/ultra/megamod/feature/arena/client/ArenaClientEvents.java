package com.ultra.megamod.feature.arena.client;

import com.ultra.megamod.feature.arena.network.ArenaCheckpointPayload;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = "megamod", value = Dist.CLIENT)
public class ArenaClientEvents {

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (!ArenaCheckpointPayload.shouldOpen) return;
        ArenaCheckpointPayload.shouldOpen = false;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        mc.setScreen(new ArenaCheckpointScreen(
                ArenaCheckpointPayload.clientWave,
                ArenaCheckpointPayload.clientReward));
    }
}
