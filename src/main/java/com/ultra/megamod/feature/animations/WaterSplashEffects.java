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
public class WaterSplashEffects {
    private static final Map<UUID, Boolean> wasInWater = new HashMap<UUID, Boolean>();
    private static final Map<UUID, Double> previousYVelocity = new HashMap<UUID, Double>();
    private static final double MIN_SPEED_FOR_ENHANCED = 0.3;
    private static final int MAX_PARTICLES = 60;
    private static final int BASE_PARTICLES = 8;

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer player2 = (ServerPlayer)player;
        UUID playerId = player2.getUUID();
        boolean currentlyInWater = player2.isInWater();
        Boolean previouslyInWater = wasInWater.get(playerId);
        Double prevYVel = previousYVelocity.get(playerId);
        wasInWater.put(playerId, currentlyInWater);
        previousYVelocity.put(playerId, player2.getDeltaMovement().y());
        if (previouslyInWater != null && !previouslyInWater.booleanValue() && currentlyInWater) {
            double fallSpeed = prevYVel != null ? Math.abs(Math.min(prevYVel, 0.0)) : 0.0;
            WaterSplashEffects.triggerSplashEffects(player2, fallSpeed);
        }
    }

    private static void triggerSplashEffects(ServerPlayer player, double fallSpeed) {
        ServerLevel level = player.level();
        double x = player.getX();
        double y = player.getY();
        double z = player.getZ();
        double intensity = Math.min(fallSpeed / 2.0, 1.0);
        int particleCount = 8 + (int)(intensity * 52.0);
        double spreadX = 0.5 + intensity * 1.5;
        double spreadZ = 0.5 + intensity * 1.5;
        double upwardSpeed = 0.2 + intensity * 0.6;
        level.sendParticles((ParticleOptions)ParticleTypes.SPLASH, x, y + 0.2, z, particleCount, spreadX, 0.1, spreadZ, upwardSpeed);
        if (fallSpeed >= 0.3) {
            int bubbleCount = 5 + (int)(intensity * 25.0);
            level.sendParticles((ParticleOptions)ParticleTypes.BUBBLE, x, y - 0.5, z, bubbleCount, 0.4 + intensity * 0.8, 0.3, 0.4 + intensity * 0.8, 0.1);
            if (intensity > 0.5) {
                int mistCount = 3 + (int)(intensity * 10.0);
                level.sendParticles((ParticleOptions)ParticleTypes.CLOUD, x, y + 0.3, z, mistCount, 0.6 + intensity * 1.0, 0.15, 0.6 + intensity * 1.0, 0.05);
            }
        }
        float volume = 0.5f + (float)intensity * 1.0f;
        float pitch = 1.0f - (float)intensity * 0.3f;
        if (fallSpeed >= 0.3) {
            level.playSound(null, player.blockPosition(), SoundEvents.PLAYER_SPLASH_HIGH_SPEED, SoundSource.PLAYERS, volume, pitch);
        } else {
            level.playSound(null, player.blockPosition(), SoundEvents.PLAYER_SPLASH, SoundSource.PLAYERS, volume, pitch);
        }
    }
}

