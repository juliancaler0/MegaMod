/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.level.Level
 */
package com.ultra.megamod.feature.museum;

import com.ultra.megamod.feature.dimensions.MegaModDimensions;
import com.ultra.megamod.feature.museum.dimension.MuseumPortalHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public class MuseumDoorItem
extends Item {
    public MuseumDoorItem(Item.Properties properties) {
        super(properties);
    }

    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (player instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer)player;
            if (level.dimension().equals(MegaModDimensions.MUSEUM)) {
                serverPlayer.sendSystemMessage((Component)Component.literal((String)"You are already in your museum! Use the portal to leave.").withStyle(ChatFormatting.YELLOW));
                return InteractionResult.FAIL;
            }
            if (level.dimension().equals(MegaModDimensions.DUNGEON)) {
                serverPlayer.sendSystemMessage((Component)Component.literal((String)"You cannot enter the museum from inside a dungeon!").withStyle(ChatFormatting.RED));
                return InteractionResult.FAIL;
            }
            MuseumPortalHandler.handleEnterMuseum(serverPlayer);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}

