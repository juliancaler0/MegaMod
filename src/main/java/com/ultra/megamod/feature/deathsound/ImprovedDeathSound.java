/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.damagesource.DamageTypes
 *  net.minecraft.world.entity.LivingEntity
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.neoforge.event.entity.living.LivingDeathEvent
 */
package com.ultra.megamod.feature.deathsound;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

@EventBusSubscriber(modid="megamod")
public class ImprovedDeathSound {
    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        float pitch;
        float volume;
        SoundEvent sound;
        LivingEntity livingEntity = event.getEntity();
        if (!(livingEntity instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer player = (ServerPlayer)livingEntity;
        DamageSource source = event.getSource();
        if (source.is(DamageTypes.FALL)) {
            sound = SoundEvents.BONE_BLOCK_BREAK;
            volume = 1.0f;
            pitch = 1.0f;
        } else if (source.is(DamageTypes.IN_FIRE) || source.is(DamageTypes.ON_FIRE) || source.is(DamageTypes.LAVA)) {
            sound = SoundEvents.FIRE_EXTINGUISH;
            volume = 1.0f;
            pitch = 1.0f;
        } else if (source.is(DamageTypes.DROWN)) {
            sound = SoundEvents.PLAYER_SPLASH_HIGH_SPEED;
            volume = 1.0f;
            pitch = 1.0f;
        } else if (source.is(DamageTypes.EXPLOSION) || source.is(DamageTypes.PLAYER_EXPLOSION)) {
            sound = (SoundEvent)SoundEvents.GENERIC_EXPLODE.value();
            volume = 1.0f;
            pitch = 1.0f;
        } else if (source.is(DamageTypes.FELL_OUT_OF_WORLD)) {
            sound = (SoundEvent)SoundEvents.AMBIENT_CAVE.value();
            volume = 1.0f;
            pitch = 1.0f;
        } else if (source.getEntity() != null && !(source.getEntity() instanceof ServerPlayer)) {
            sound = (SoundEvent)SoundEvents.RAID_HORN.value();
            volume = 0.3f;
            pitch = 1.0f;
        } else {
            sound = SoundEvents.TOTEM_USE;
            volume = 0.8f;
            pitch = 0.6f;
        }
        player.level().playSound(null, player.blockPosition(), sound, SoundSource.PLAYERS, volume, pitch);
    }
}

