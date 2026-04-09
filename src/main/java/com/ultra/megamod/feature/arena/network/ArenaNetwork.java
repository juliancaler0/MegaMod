package com.ultra.megamod.feature.arena.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class ArenaNetwork {
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("megamod");
        registrar.playToClient(ArenaCheckpointPayload.TYPE, ArenaCheckpointPayload.STREAM_CODEC, ArenaCheckpointPayload::handleClient);
        registrar.playToServer(ArenaCheckpointResponsePayload.TYPE, ArenaCheckpointResponsePayload.STREAM_CODEC, ArenaCheckpointResponsePayload::handleServer);
        registrar.playToClient(ArenaHudSyncPayload.TYPE, ArenaHudSyncPayload.STREAM_CODEC, ArenaHudSyncPayload::handleClient);
    }
}
