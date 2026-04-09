/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Registry
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.resources.Identifier
 *  net.minecraft.sounds.SoundEvent
 *  net.neoforged.neoforge.registries.DeferredHolder
 *  net.neoforged.neoforge.registries.DeferredRegister
 */
package com.ultra.megamod.feature.dungeons;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class DungeonSoundRegistry {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create((Registry)BuiltInRegistries.SOUND_EVENT, (String)"megamod");
    public static final DeferredHolder<SoundEvent, SoundEvent> BOSS_FIGHT_MUSIC = SOUNDS.register("boss_fight_music", () -> SoundEvent.createVariableRangeEvent((Identifier)Identifier.fromNamespaceAndPath((String)"megamod", (String)"boss_fight_music")));
    public static final DeferredHolder<SoundEvent, SoundEvent> BOSS_DASH_SFX = SOUNDS.register("boss_dash", () -> SoundEvent.createVariableRangeEvent((Identifier)Identifier.fromNamespaceAndPath((String)"megamod", (String)"boss_dash")));
    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DISC_HOUSE_MONEY = SOUNDS.register("music_disc.house_money", () -> SoundEvent.createVariableRangeEvent((Identifier)Identifier.fromNamespaceAndPath((String)"megamod", (String)"music_disc.house_money")));
}

