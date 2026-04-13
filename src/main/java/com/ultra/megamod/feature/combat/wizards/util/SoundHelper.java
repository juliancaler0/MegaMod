package com.ultra.megamod.feature.combat.wizards.util;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class SoundHelper {
    public static void playSoundEvent(Level level, Entity entity, SoundEvent soundEvent) {
        playSoundEvent(level, entity, soundEvent, 1, 1);
    }

    public static void playSoundEvent(Level level, Entity entity, SoundEvent soundEvent, float volume, float pitch) {
        level.playSound(
                (Player) null,
                entity.getX(),
                entity.getY(),
                entity.getZ(),
                soundEvent,
                SoundSource.PLAYERS,
                volume,
                pitch);
    }
}
