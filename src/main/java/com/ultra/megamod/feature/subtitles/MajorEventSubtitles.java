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
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.boss.wither.WitherBoss
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.AABB
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.neoforge.event.entity.EntityJoinLevelEvent
 *  net.neoforged.neoforge.event.entity.player.PlayerEvent$PlayerChangedDimensionEvent
 */
package com.ultra.megamod.feature.subtitles;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid="megamod")
public class MajorEventSubtitles {
    private static final int FADE_IN = 20;
    private static final int STAY = 60;
    private static final int FADE_OUT = 20;
    private static final double WITHER_NOTIFY_RANGE = 128.0;

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        WitherBoss wither;
        if (event.getLevel().isClientSide()) {
            return;
        }
        Entity entity = event.getEntity();
        if (entity instanceof WitherBoss && (wither = (WitherBoss)entity).getInvulnerableTicks() > 0) {
            ServerLevel level = (ServerLevel)event.getLevel();
            AABB notifyBox = wither.getBoundingBox().inflate(128.0);
            List<ServerPlayer> nearbyPlayers = level.getEntitiesOfClass(ServerPlayer.class, notifyBox);
            MutableComponent title = Component.literal("The Wither Has Been Summoned").withStyle(new ChatFormatting[]{ChatFormatting.DARK_RED, ChatFormatting.BOLD});
            MutableComponent subtitle = Component.literal("Prepare yourself...").withStyle(ChatFormatting.GRAY);
            for (ServerPlayer player : nearbyPlayers) {
                MajorEventSubtitles.sendTitle(player, title, subtitle);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        MutableComponent subtitle;
        MutableComponent title;
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer player2 = (ServerPlayer)player;
        if (event.getTo() == Level.END) {
            title = Component.literal("The End").withStyle(new ChatFormatting[]{ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD});
            subtitle = Component.literal("The void awaits...").withStyle(ChatFormatting.GRAY);
            MajorEventSubtitles.sendTitle(player2, title, subtitle);
        }
        if (event.getTo() == Level.NETHER) {
            title = Component.literal("The Nether").withStyle(new ChatFormatting[]{ChatFormatting.RED, ChatFormatting.BOLD});
            subtitle = Component.literal("Turn back while you can...").withStyle(ChatFormatting.GRAY);
            MajorEventSubtitles.sendTitle(player2, title, subtitle);
        }
    }

    private static void sendTitle(ServerPlayer player, Component title, Component subtitle) {
        player.connection.send(new ClientboundSetTitlesAnimationPacket(20, 60, 20));
        player.connection.send(new ClientboundSetSubtitleTextPacket(subtitle));
        player.connection.send(new ClientboundSetTitleTextPacket(title));
    }
}

