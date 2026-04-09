package com.ultra.megamod.feature.dungeons.client;

import com.ultra.megamod.feature.dungeons.DungeonSoundRegistry;
import com.ultra.megamod.feature.dungeons.network.BossMusicPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Client-only handler for boss music payloads.
 * Plays/stops boss music when entering/leaving boss fights.
 */
public class BossMusicClientHandler {
    public static final Map<Integer, SimpleSoundInstance> activeTracks = new HashMap<>();

    public static void handle(BossMusicPayload payload) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        int entityId = payload.entityId();
        boolean play = payload.play();

        if (play) {
            // Stop any existing track for this entity
            SimpleSoundInstance existing = activeTracks.remove(entityId);
            if (existing != null) {
                mc.getSoundManager().stop(existing);
            }
            // Start new boss music
            SoundEvent bossMusic = DungeonSoundRegistry.BOSS_FIGHT_MUSIC.get();
            SimpleSoundInstance instance = SimpleSoundInstance.forMusic(bossMusic);
            activeTracks.put(entityId, instance);
            mc.getSoundManager().play(instance);
        } else {
            // Stop boss music for this entity
            SimpleSoundInstance track = activeTracks.remove(entityId);
            if (track != null) {
                mc.getSoundManager().stop(track);
            }
        }
    }
}
