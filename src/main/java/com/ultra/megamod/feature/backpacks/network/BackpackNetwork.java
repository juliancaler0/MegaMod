package com.ultra.megamod.feature.backpacks.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class BackpackNetwork {

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("megamod");

        registrar.playToServer(
            OpenBackpackPayload.TYPE,
            OpenBackpackPayload.STREAM_CODEC,
            OpenBackpackPayload::handleOnServer
        );
        registrar.playToServer(
            BackpackActionPayload.TYPE,
            BackpackActionPayload.STREAM_CODEC,
            BackpackActionPayload::handleOnServer
        );
        registrar.playToClient(
            BackpackSyncPayload.TYPE,
            BackpackSyncPayload.STREAM_CODEC,
            BackpackSyncPayload::handleOnClient
        );
    }
}
