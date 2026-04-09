/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  net.minecraft.commands.CommandSourceStack
 *  net.minecraft.commands.Commands
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.level.gamerules.GameRules
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.neoforge.event.RegisterCommandsEvent
 */
package com.ultra.megamod.feature.multiplayer;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.gamerules.GameRules;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid="megamod")
public class ViewableGamerules {
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register((LiteralArgumentBuilder)Commands.literal((String)"megamod").then(Commands.literal((String)"gamerules").executes(context -> {
            ServerPlayer player = ((CommandSourceStack)context.getSource()).getPlayerOrException();
            ServerLevel level = (ServerLevel) player.level();
            GameRules gameRules = level.getGameRules();
            player.sendSystemMessage((Component)Component.literal((String)"\u00a76=== Server Gamerules ==="));
            gameRules.availableRules().forEach(rule -> {
                String name = rule.id();
                String value = gameRules.getAsString(rule);
                player.sendSystemMessage((Component)Component.literal((String)("\u00a76" + name + "\u00a7r: \u00a7a" + value + "\u00a7r")));
            });
            return 1;
        })));
    }
}

