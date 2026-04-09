/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  net.minecraft.commands.CommandSourceStack
 *  net.minecraft.commands.Commands
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.level.GameType
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.neoforge.event.RegisterCommandsEvent
 */
package com.ultra.megamod.feature.multiplayer;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid="megamod")
public class PlayerlistCommand {
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register((LiteralArgumentBuilder)Commands.literal((String)"megamod").then(Commands.literal((String)"players").executes(context -> {
            ServerPlayer sender = ((CommandSourceStack)context.getSource()).getPlayerOrException();
            MinecraftServer server = ((CommandSourceStack)context.getSource()).getServer();
            List<ServerPlayer> onlinePlayers = server.getPlayerList().getPlayers();
            int online = onlinePlayers.size();
            int max = server.getPlayerList().getMaxPlayers();
            sender.sendSystemMessage((Component)Component.literal((String)("\u00a76=== Online Players (" + online + "/" + max + ") ===")));
            LinkedHashMap<GameType, List> grouped = new LinkedHashMap<GameType, List>();
            grouped.put(GameType.SURVIVAL, new ArrayList());
            grouped.put(GameType.CREATIVE, new ArrayList());
            grouped.put(GameType.ADVENTURE, new ArrayList());
            grouped.put(GameType.SPECTATOR, new ArrayList());
            for (ServerPlayer serverPlayer : onlinePlayers) {
                GameType mode = serverPlayer.gameMode.getGameModeForPlayer();
                grouped.computeIfAbsent(mode, k -> new ArrayList()).add(serverPlayer);
            }
            for (Map.Entry entry : grouped.entrySet()) {
                List players = (List)entry.getValue();
                if (players.isEmpty()) continue;
                String modeName = PlayerlistCommand.capitalize(((GameType)entry.getKey()).getName());
                StringBuilder line = new StringBuilder();
                line.append("\u00a7a[").append(modeName).append("]\u00a7r: ");
                for (int i = 0; i < players.size(); ++i) {
                    ServerPlayer player = (ServerPlayer)players.get(i);
                    String name = player.getGameProfile().name();
                    int latency = player.connection.latency();
                    String pingColor = PlayerlistCommand.getPingColor(latency);
                    if (i > 0) {
                        line.append(", ");
                    }
                    line.append(name).append(" ").append(pingColor).append("(").append(latency).append("ms)").append("\u00a7r");
                }
                sender.sendSystemMessage((Component)Component.literal((String)line.toString()));
            }
            return 1;
        })));
    }

    private static String getPingColor(int latency) {
        if (latency < 100) {
            return "\u00a7a";
        }
        if (latency < 300) {
            return "\u00a7e";
        }
        return "\u00a7c";
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) {
            return s;
        }
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}

