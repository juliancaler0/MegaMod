package com.ultra.megamod.feature.sorting.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class SortNetwork {
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("megamod");
        registrar.playToServer(SortActionPayload.TYPE, SortActionPayload.STREAM_CODEC, SortActionPayload::handleOnServer);
    }
}
