package com.ultra.megamod.feature.map.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class MapTileSyncNetwork {

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("megamod");

        registrar.playToServer(
                MapTileUploadPayload.TYPE,
                MapTileUploadPayload.STREAM_CODEC,
                MapTileUploadPayload::handleOnServer
        );
        registrar.playToServer(
                MapTileRequestPayload.TYPE,
                MapTileRequestPayload.STREAM_CODEC,
                MapTileRequestPayload::handleOnServer
        );
        registrar.playToClient(
                MapTileDataPayload.TYPE,
                MapTileDataPayload.STREAM_CODEC,
                MapTileDataPayload::handleOnClient
        );
    }
}
