package com.ultra.megamod.feature.combat.spell;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Registers network payloads for the spell casting system.
 */
public class SpellNetwork {

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("megamod");
        registrar.playToClient(SpellCastSyncPayload.TYPE, SpellCastSyncPayload.STREAM_CODEC,
            SpellCastSyncPayload::handleOnClient);
        registrar.playToClient(NearbyPlayerCastPayload.TYPE, NearbyPlayerCastPayload.STREAM_CODEC,
            NearbyPlayerCastPayload::handleOnClient);
        registrar.playToServer(SpellBookCastPayload.TYPE, SpellBookCastPayload.STREAM_CODEC,
            SpellBookCastPayload::handleOnServer);
        registrar.playToClient(BeamSyncPayload.TYPE, BeamSyncPayload.STREAM_CODEC,
            BeamSyncPayload::handleOnClient);
    }
}
