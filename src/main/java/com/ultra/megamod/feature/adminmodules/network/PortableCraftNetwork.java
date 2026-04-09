package com.ultra.megamod.feature.adminmodules.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class PortableCraftNetwork {
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("megamod");
        registrar.playToServer(PortableCraftPayload.TYPE, PortableCraftPayload.STREAM_CODEC, PortableCraftPayload::handleOnServer);
    }
}
