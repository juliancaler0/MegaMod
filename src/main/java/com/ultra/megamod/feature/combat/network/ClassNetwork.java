package com.ultra.megamod.feature.combat.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Registers network payloads for the class selection system.
 */
public class ClassNetwork {

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("megamod");

        // S2C: tell client to open class selection screen
        registrar.playToClient(ClassSelectionPayload.TYPE, ClassSelectionPayload.STREAM_CODEC,
                ClassSelectionPayload::handleOnClient);

        // S2C: sync player's class to client
        registrar.playToClient(ClassSyncPayload.TYPE, ClassSyncPayload.STREAM_CODEC,
                ClassSyncPayload::handleOnClient);

        // C2S: player chose a class
        registrar.playToServer(ClassChoicePayload.TYPE, ClassChoicePayload.STREAM_CODEC,
                ClassChoicePayload::handleOnServer);
    }
}
