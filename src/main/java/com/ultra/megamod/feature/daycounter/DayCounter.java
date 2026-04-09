/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket
 *  net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket
 *  net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.Level
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.neoforge.event.entity.player.PlayerEvent$PlayerLoggedInEvent
 *  net.neoforged.neoforge.event.tick.LevelTickEvent$Post
 */
package com.ultra.megamod.feature.daycounter;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

@EventBusSubscriber(modid="megamod")
public class DayCounter {
    private static long lastAnnouncedDay = -1L;

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer player2 = (ServerPlayer)player;
        ServerLevel level = player2.level();
        long currentDay = DayCounter.getCurrentDay(level);
        DayCounter.sendDayTitle(player2, currentDay);
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        Level level = event.getLevel();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel level2 = (ServerLevel)level;
        if (level2.dimension() != ServerLevel.OVERWORLD) {
            return;
        }
        long currentDay = DayCounter.getCurrentDay(level2);
        if (currentDay != lastAnnouncedDay && lastAnnouncedDay != -1L) {
            lastAnnouncedDay = currentDay;
            for (ServerPlayer player : level2.getServer().getPlayerList().getPlayers()) {
                DayCounter.sendDayTitle(player, currentDay);
            }
        }
        if (lastAnnouncedDay == -1L) {
            lastAnnouncedDay = currentDay;
        }
    }

    private static long getCurrentDay(ServerLevel level) {
        return level.getDayTime() / 24000L + 1L;
    }

    private static void sendDayTitle(ServerPlayer player, long day) {
        MutableComponent subtitle;
        MutableComponent title;
        boolean isMilestone;
        boolean bl = isMilestone = day % 100L == 0L;
        if (isMilestone) {
            title = Component.literal((String)("Day " + day)).withStyle(new ChatFormatting[]{ChatFormatting.GOLD, ChatFormatting.BOLD});
            subtitle = Component.literal((String)"A milestone has been reached!").withStyle(ChatFormatting.YELLOW);
            player.connection.send((Packet)new ClientboundSetTitlesAnimationPacket(20, 80, 20));
        } else {
            title = Component.literal((String)("Day " + day)).withStyle(ChatFormatting.WHITE);
            subtitle = Component.empty();
            player.connection.send((Packet)new ClientboundSetTitlesAnimationPacket(10, 40, 10));
        }
        player.connection.send((Packet)new ClientboundSetSubtitleTextPacket((Component)subtitle));
        player.connection.send((Packet)new ClientboundSetTitleTextPacket((Component)title));
    }
}

