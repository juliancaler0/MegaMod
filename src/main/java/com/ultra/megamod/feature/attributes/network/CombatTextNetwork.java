package com.ultra.megamod.feature.attributes.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class CombatTextNetwork {
    private CombatTextNetwork() {}

    public static void registerPayloads(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("megamod").versioned("1.0");
        registrar.playToClient(
                CombatTextPayload.TYPE,
                CombatTextPayload.STREAM_CODEC,
                CombatTextPayload::handleOnClient);
    }
}
