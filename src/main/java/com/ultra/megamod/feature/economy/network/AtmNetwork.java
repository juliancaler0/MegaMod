package com.ultra.megamod.feature.economy.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class AtmNetwork {
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("megamod");
        registrar.playToClient(OpenAtmPayload.TYPE, OpenAtmPayload.STREAM_CODEC, OpenAtmPayload::handleOnClient);
        registrar.playToClient(PlayerInfoSyncPayload.TYPE, PlayerInfoSyncPayload.STREAM_CODEC, PlayerInfoSyncPayload::handleOnClient);
    }
}
