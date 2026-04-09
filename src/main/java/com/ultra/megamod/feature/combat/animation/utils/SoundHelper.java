package com.ultra.megamod.feature.combat.animation.utils;

import com.ultra.megamod.feature.combat.animation.WeaponAttributes;
import com.ultra.megamod.feature.combat.animation.config.BetterCombatConfig;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;

/**
 * Swing sound playback utility.
 * Ported from BetterCombat (net.bettercombat.utils.SoundHelper).
 */
public class SoundHelper {

    public static void playSound(Player player, WeaponAttributes.Sound sound) {
        if (sound == null || sound.id() == null || sound.id().isEmpty()) return;
        float volume = sound.volume() * (BetterCombatConfig.weaponSwingSoundVolume / 100f);
        if (volume <= 0) return;

        float pitch = sound.pitch() + (player.getRandom().nextFloat() - 0.5f) * 2f * sound.randomness();

        try {
            var soundId = Identifier.fromNamespaceAndPath("megamod", sound.id().replace(".", "/"));
            var soundEvent = SoundEvent.createVariableRangeEvent(soundId);
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    soundEvent, SoundSource.PLAYERS, volume, pitch);
        } catch (Exception ignored) {}
    }
}
