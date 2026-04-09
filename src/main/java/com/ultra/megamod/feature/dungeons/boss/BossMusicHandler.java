/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.resources.sounds.SimpleSoundInstance
 *  net.minecraft.client.resources.sounds.SoundInstance
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.neoforge.client.event.ClientTickEvent$Post
 */
package com.ultra.megamod.feature.dungeons.boss;

import com.ultra.megamod.feature.dungeons.network.BossMusicPayload;
import com.ultra.megamod.feature.dungeons.network.DungeonSyncPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid="megamod", value={Dist.CLIENT})
public class BossMusicHandler {
    private static boolean wasInBossFight = false;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        boolean inBossFight;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        boolean bl = inBossFight = DungeonSyncPayload.clientInDungeon && DungeonSyncPayload.clientBossAlive;
        if (inBossFight && !wasInBossFight) {
            mc.getMusicManager().stopPlaying();
            wasInBossFight = true;
        }
        if (inBossFight) {
            mc.getMusicManager().stopPlaying();
        }
        if (!inBossFight && wasInBossFight) {
            wasInBossFight = false;
            for (SimpleSoundInstance sound : com.ultra.megamod.feature.dungeons.client.BossMusicClientHandler.activeTracks.values()) {
                mc.getSoundManager().stop((SoundInstance)sound);
            }
            com.ultra.megamod.feature.dungeons.client.BossMusicClientHandler.activeTracks.clear();
        }
    }
}

