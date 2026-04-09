package com.ultra.megamod.feature.furniture;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class QuestBoardNetwork {

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("megamod").versioned("1.0").optional();

        registrar.playToServer(
            QuestBoardActionPayload.TYPE,
            QuestBoardActionPayload.STREAM_CODEC,
            (payload, context) -> context.enqueueWork(() -> {
                if (context.player() instanceof ServerPlayer player) {
                    QuestBoardHandler.handleAction(player, payload.action(), payload.jsonData());
                }
            })
        );

        registrar.playToClient(
            QuestBoardDataPayload.TYPE,
            QuestBoardDataPayload.STREAM_CODEC,
            (payload, context) -> context.enqueueWork(() -> {
                QuestBoardDataPayload.lastResponse = payload;
            })
        );
    }
}
