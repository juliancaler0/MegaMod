package com.ultra.megamod.lib.spellengine.utils;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import com.ultra.megamod.lib.spellengine.api.spell.fx.Sound;

public class SoundHelper {
    public static void playSound(Level world, Entity entity, Sound sound) {
        if (sound == null) {
            return;
        }
        try {
            var soundEventOpt = BuiltInRegistries.SOUND_EVENT.get(Identifier.parse(sound.id()));
            soundEventOpt.ifPresent(ref -> playSoundEvent(world, entity, ref.value(), sound.volume(), sound.randomizedPitch()));
        } catch (Exception e) {
            System.err.println("Failed to play sound: " + sound.id());
            e.printStackTrace();
        }
    }

    public static void playSoundEvent(Level world, Entity entity, SoundEvent soundEvent) {
        playSoundEvent(world, entity, soundEvent, 1, 1);
    }

    public static void playSoundEvent(Level world, Entity entity, SoundEvent soundEvent, float volume, float pitch) {
        world.playSound(
                (Player)null,
                entity.getX(),
                entity.getY(),
                entity.getZ(),
                soundEvent,
                SoundSource.PLAYERS,
                volume,
                pitch);
    }
}
