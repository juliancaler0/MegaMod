package com.ultra.megamod.feature.corruption.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Registers corruption network payloads.
 * Called from MegaMod constructor: modEventBus.addListener(CorruptionNetwork::registerPayloads);
 */
public class CorruptionNetwork {
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("megamod");
        registrar.playToClient(CorruptionSyncPayload.TYPE, CorruptionSyncPayload.STREAM_CODEC,
                CorruptionSyncPayload::handleOnClient);
        registrar.playToClient(CorruptionZoneSyncPayload.TYPE, CorruptionZoneSyncPayload.STREAM_CODEC,
                CorruptionZoneSyncPayload::handleOnClient);
    }
}
