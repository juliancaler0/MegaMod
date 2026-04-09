package com.ultra.megamod.feature.alchemy.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class AlchemyNetwork {
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("megamod");
        registrar.playToServer(AlchemyCauldronPayload.TYPE, AlchemyCauldronPayload.STREAM_CODEC, AlchemyCauldronPayload::handleOnServer);
        registrar.playToClient(AlchemyCauldronSyncPayload.TYPE, AlchemyCauldronSyncPayload.STREAM_CODEC, AlchemyCauldronSyncPayload::handleOnClient);
        registrar.playToServer(AlchemyGrindstonePayload.TYPE, AlchemyGrindstonePayload.STREAM_CODEC, AlchemyGrindstonePayload::handleOnServer);
        registrar.playToClient(AlchemyGrindstoneSyncPayload.TYPE, AlchemyGrindstoneSyncPayload.STREAM_CODEC, AlchemyGrindstoneSyncPayload::handleOnClient);
    }
}
