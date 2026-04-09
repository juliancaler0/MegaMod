/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.entity.player.Player
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.neoforge.event.entity.player.PlayerEvent$PlayerLoggedInEvent
 *  net.neoforged.neoforge.event.entity.player.PlayerEvent$PlayerLoggedOutEvent
 */
package com.ultra.megamod.feature.multiplayer;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid="megamod")
public class JoinExitSounds {
    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer joiningPlayer = (ServerPlayer)player;
        MinecraftServer server = joiningPlayer.level().getServer();
        if (server == null) {
            return;
        }
        String playerName = joiningPlayer.getGameProfile().name();
        MutableComponent joinMessage = Component.literal((String)("[+] " + playerName + " joined")).withStyle(ChatFormatting.GREEN);
        for (ServerPlayer onlinePlayer : server.getPlayerList().getPlayers()) {
            if (onlinePlayer == joiningPlayer) continue;
            onlinePlayer.level().playSound(null, onlinePlayer.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.4f, 1.8f);
            onlinePlayer.sendSystemMessage((Component)joinMessage);
        }
    }

    @SubscribeEvent
    public static void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent event) {
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer leavingPlayer = (ServerPlayer)player;
        MinecraftServer server = leavingPlayer.level().getServer();
        if (server == null) {
            return;
        }
        String playerName = leavingPlayer.getGameProfile().name();
        MutableComponent leaveMessage = Component.literal((String)("[-] " + playerName + " left")).withStyle(ChatFormatting.RED);
        for (ServerPlayer onlinePlayer : server.getPlayerList().getPlayers()) {
            if (onlinePlayer == leavingPlayer) continue;
            onlinePlayer.level().playSound(null, onlinePlayer.blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.6f, 0.7f);
            onlinePlayer.sendSystemMessage((Component)leaveMessage);
        }
    }
}

