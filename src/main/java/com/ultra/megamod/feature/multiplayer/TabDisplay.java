/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.player.Player
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.neoforge.event.entity.player.PlayerEvent$TabListNameFormat
 */
package com.ultra.megamod.feature.multiplayer;

import com.ultra.megamod.feature.multiplayer.PlayerStatistics;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid="megamod")
public class TabDisplay {
    @SubscribeEvent
    public static void onTabListNameFormat(PlayerEvent.TabListNameFormat event) {
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer serverPlayer = (ServerPlayer) player;
        PlayerStatistics stats = PlayerStatistics.getIfLoaded();
        if (stats == null) {
            return;
        }
        int kills = stats.getStat(serverPlayer.getUUID(), "kills");
        int deaths = stats.getStat(serverPlayer.getUUID(), "deaths");
        ChatFormatting kdColor = kills >= deaths ? ChatFormatting.GREEN : ChatFormatting.RED;
        String playerName = serverPlayer.getGameProfile().name();

        MutableComponent displayName = Component.literal(playerName).withStyle(ChatFormatting.WHITE);

        // Class suffix retired with the class-selection system.

        displayName.append(Component.literal(" ").withStyle(ChatFormatting.GRAY))
                   .append(Component.literal("[K:" + kills + " D:" + deaths + "]").withStyle(kdColor));

        event.setDisplayName(displayName);
    }
}

