/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.entity.player.Player
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.neoforge.event.tick.PlayerTickEvent$Post
 */
package com.ultra.megamod.feature.animations;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid="megamod")
public class PlayerLevelUpEffects {
    private static final Map<UUID, Integer> previousLevels = new HashMap<UUID, Integer>();
    private static final int SPIRAL_DURATION_TICKS = 20;
    private static final Map<UUID, Integer> activeSpiralEffects = new HashMap<UUID, Integer>();

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer player2 = (ServerPlayer)player;
        UUID playerId = player2.getUUID();
        int currentLevel = player2.experienceLevel;
        Integer previousLevel = previousLevels.get(playerId);
        previousLevels.put(playerId, currentLevel);
        if (previousLevel != null && currentLevel > previousLevel) {
            PlayerLevelUpEffects.triggerLevelUpEffects(player2);
        }
        PlayerLevelUpEffects.updateSpiralEffect(player2);
    }

    private static void triggerLevelUpEffects(ServerPlayer player) {
        ServerLevel level = player.level();
        double x = player.getX();
        double y = player.getY();
        double z = player.getZ();
        level.sendParticles((ParticleOptions)ParticleTypes.TOTEM_OF_UNDYING, x, y + 1.0, z, 40, 0.8, 1.0, 0.8, 0.5);
        level.sendParticles((ParticleOptions)ParticleTypes.ENCHANT, x, y + 1.5, z, 20, 0.6, 0.8, 0.6, 1.0);
        level.playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0f, 1.0f);
        level.playSound(null, player.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.8f, 1.2f);
        activeSpiralEffects.put(player.getUUID(), 20);
    }

    private static void updateSpiralEffect(ServerPlayer player) {
        int newRemaining;
        UUID playerId = player.getUUID();
        Integer remainingTicks = activeSpiralEffects.get(playerId);
        if (remainingTicks == null || remainingTicks <= 0) {
            return;
        }
        ServerLevel level = player.level();
        int elapsed = 20 - remainingTicks;
        double progress = (double)elapsed / 20.0;
        double height = progress * 2.5;
        double angle = (double)elapsed * 0.5;
        double radius = 0.8 * (1.0 - progress * 0.3);
        double x = player.getX();
        double y = player.getY();
        double z = player.getZ();
        for (int arm = 0; arm < 2; ++arm) {
            double armAngle = angle + (double)arm * Math.PI;
            double particleX = x + Math.cos(armAngle) * radius;
            double particleZ = z + Math.sin(armAngle) * radius;
            double particleY = y + height;
            level.sendParticles((ParticleOptions)ParticleTypes.TOTEM_OF_UNDYING, particleX, particleY, particleZ, 2, 0.05, 0.05, 0.05, 0.01);
        }
        if (elapsed % 3 == 0) {
            level.sendParticles((ParticleOptions)ParticleTypes.END_ROD, x, y + height, z, 1, 0.1, 0.1, 0.1, 0.02);
        }
        if ((newRemaining = remainingTicks - 1) <= 0) {
            activeSpiralEffects.remove(playerId);
        } else {
            activeSpiralEffects.put(playerId, newRemaining);
        }
    }
}

