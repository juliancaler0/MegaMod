package com.ultra.megamod.feature.citizen.blueprint.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Registers all blueprint-related network payloads.
 * Called from MegaMod main constructor via {@code modEventBus.addListener(BlueprintNetwork::registerPayloads)}.
 */
public class BlueprintNetwork {

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("megamod_blueprint").versioned("1.0").optional();

        // Server -> Client: sync blueprint data for ghost rendering preview
        registrar.playToClient(BlueprintSyncPayload.TYPE,
                BlueprintSyncPayload.STREAM_CODEC,
                BlueprintSyncPayload::handleOnClient);

        // Client -> Server: request placement of a blueprint from a structure pack
        registrar.playToServer(BuildToolPlacePayload.TYPE,
                BuildToolPlacePayload.STREAM_CODEC,
                BuildToolPlacePayload::handleOnServer);

        // Client -> Server: save scanned region as a blueprint file
        registrar.playToServer(ScanSavePayload.TYPE,
                ScanSavePayload.STREAM_CODEC,
                ScanSavePayload::handleOnServer);

        // Client -> Server: place a supply camp or ship from a blueprint pack
        registrar.playToServer(SupplyPlacePayload.TYPE,
                SupplyPlacePayload.STREAM_CODEC,
                SupplyPlacePayload::handleOnServer);
    }
}
