package com.ultra.megamod.lib.azurelib.common.platform.services;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import com.ultra.megamod.lib.azurelib.AzureLib;
import com.ultra.megamod.lib.azurelib.common.network.AbstractPacket;

public interface AzureLibNetwork {

    Identifier AZ_ITEM_STACK_DISPATCH_COMMAND_SYNC_PACKET_ID = AzureLib.modResource(
        "az_item_stack_dispatch_command_sync"
    );

    void sendToTrackingEntityAndSelf(AbstractPacket packet, Entity entityToTrack);

    void sendToEntitiesTrackingChunk(AbstractPacket packet, ServerLevel level, BlockPos blockPos);

    void sendToPlayer(AbstractPacket packet, ServerPlayer player);
}
