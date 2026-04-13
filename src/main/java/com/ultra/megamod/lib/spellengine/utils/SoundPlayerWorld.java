package com.ultra.megamod.lib.spellengine.utils;

import net.minecraft.world.entity.Entity;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;

public interface SoundPlayerWorld {
    void playSoundFromEntity(Entity entity, SoundEvent sound, SoundSource category, float volume, float pitch);
}
