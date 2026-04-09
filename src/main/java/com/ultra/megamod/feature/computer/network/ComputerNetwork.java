package com.ultra.megamod.feature.computer.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class ComputerNetwork {
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("megamod");
        registrar.playToClient(OpenComputerPayload.TYPE, OpenComputerPayload.STREAM_CODEC, OpenComputerPayload::handleOnClient);
        registrar.playToServer(ComputerActionPayload.TYPE, ComputerActionPayload.STREAM_CODEC, ComputerActionPayload::handleOnServer);
        registrar.playToClient(ComputerDataPayload.TYPE, ComputerDataPayload.STREAM_CODEC, ComputerDataPayload::handleOnClient);
    }
}
