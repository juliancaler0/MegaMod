package com.ultra.megamod.feature.dungeons.insurance.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class InsuranceNetwork {
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("megamod");
        registrar.playToClient(InsuranceOpenPayload.TYPE, InsuranceOpenPayload.STREAM_CODEC, InsuranceOpenPayload::handleOnClient);
        registrar.playToServer(InsuranceReadyPayload.TYPE, InsuranceReadyPayload.STREAM_CODEC, InsuranceReadyPayload::handleOnServer);
        registrar.playToClient(InsuranceStatusPayload.TYPE, InsuranceStatusPayload.STREAM_CODEC, InsuranceStatusPayload::handleOnClient);
    }
}
