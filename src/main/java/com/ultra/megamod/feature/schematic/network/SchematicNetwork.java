package com.ultra.megamod.feature.schematic.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Registers all schematic-related network payloads.
 */
public class SchematicNetwork {

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("megamod_schematic");

        // Client -> Server: placement confirmation with schematic data
        registrar.playToServer(SchematicPlacementPayload.TYPE,
                SchematicPlacementPayload.STREAM_CODEC,
                SchematicPlacementPayload::handleOnServer);

        // Client -> Server: build order management (assign/cancel)
        registrar.playToServer(BuildOrderPayload.TYPE,
                BuildOrderPayload.STREAM_CODEC,
                BuildOrderPayload::handleOnServer);

        // Server -> Client: build progress updates
        registrar.playToClient(BuildProgressPayload.TYPE,
                BuildProgressPayload.STREAM_CODEC,
                BuildProgressPayload::handleOnClient);
    }
}
