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

import com.ultra.megamod.feature.combat.PlayerClassManager;
import com.ultra.megamod.feature.combat.PlayerClassManager.PlayerClass;
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

        // Append player class in the class's color
        try {
            ServerLevel overworld = serverPlayer.level().getServer().overworld();
            PlayerClassManager classManager = PlayerClassManager.get(overworld);
            PlayerClass cls = classManager.getPlayerClass(serverPlayer.getUUID());
            if (cls != PlayerClass.NONE) {
                String classIcon = getClassIcon(cls);
                displayName.append(Component.literal(" ")
                    .append(Component.literal(classIcon + cls.getDisplayName())
                        .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(cls.getColor())))));
            }
        } catch (Exception ignored) {
            // PlayerClassManager may not be initialized yet
        }

        displayName.append(Component.literal(" ").withStyle(ChatFormatting.GRAY))
                   .append(Component.literal("[K:" + kills + " D:" + deaths + "]").withStyle(kdColor));

        event.setDisplayName(displayName);
    }

    private static String getClassIcon(PlayerClass cls) {
        return switch (cls) {
            case PALADIN -> "\u2694 "; // crossed swords
            case WARRIOR -> "\u2694 ";
            case WIZARD -> "\u2726 "; // four-pointed star
            case ROGUE -> "\u2020 ";  // dagger
            case RANGER -> "\u27B3 "; // arrow
            default -> "";
        };
    }
}

