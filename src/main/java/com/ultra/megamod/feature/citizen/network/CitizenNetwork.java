package com.ultra.megamod.feature.citizen.network;

import com.ultra.megamod.feature.citizen.network.handlers.ColonySyncHandler;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class CitizenNetwork {

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("megamod").versioned("1.0").optional();

        // ---- Existing citizen entity-level payloads ----

        registrar.playToServer(
            CitizenActionPayload.TYPE,
            CitizenActionPayload.STREAM_CODEC,
            (payload, context) -> context.enqueueWork(() -> {
                if (context.player() instanceof ServerPlayer player) {
                    CitizenInteractionHandler.handleAction(player, payload.entityId(), payload.action(), payload.jsonData());
                }
            })
        );

        registrar.playToClient(
            CitizenDataPayload.TYPE,
            CitizenDataPayload.STREAM_CODEC,
            (payload, context) -> context.enqueueWork(() -> {
                CitizenDataPayload.lastResponse = payload;
            })
        );

        // ---- Colony view sync (server -> client) ----

        registrar.playToClient(
            ColonyViewPayload.TYPE,
            ColonyViewPayload.STREAM_CODEC,
            (payload, context) -> context.enqueueWork(() -> {
                ColonyViewPayload.lastResponse = payload;
            })
        );

        // ---- Building view sync (server -> client) ----

        registrar.playToClient(
            BuildingViewPayload.TYPE,
            BuildingViewPayload.STREAM_CODEC,
            (payload, context) -> context.enqueueWork(() -> {
                BuildingViewPayload.lastResponse = payload;
            })
        );

        // ---- Colony actions (client -> server) ----

        registrar.playToServer(
            ColonyActionPayload.TYPE,
            ColonyActionPayload.STREAM_CODEC,
            (payload, context) -> context.enqueueWork(() -> {
                if (context.player() instanceof ServerPlayer player) {
                    ColonySyncHandler.handleColonyAction(player, payload.colonyId(), payload.action(), payload.jsonData());
                }
            })
        );

        // ---- Building actions (client -> server) ----

        registrar.playToServer(
            BuildingActionPayload.TYPE,
            BuildingActionPayload.STREAM_CODEC,
            (payload, context) -> context.enqueueWork(() -> {
                if (context.player() instanceof ServerPlayer player) {
                    ColonySyncHandler.handleBuildingAction(player, payload.colonyId(), payload.buildingPos(), payload.action(), payload.jsonData());
                }
            })
        );

        // ---- Citizen detail sync (server -> client) ----

        registrar.playToClient(
            CitizenSyncPayload.TYPE,
            CitizenSyncPayload.STREAM_CODEC,
            (payload, context) -> context.enqueueWork(() -> {
                CitizenSyncPayload.lastResponse = payload;
            })
        );

        // ---- Work order actions (client -> server) ----

        registrar.playToServer(
            WorkOrderPayload.TYPE,
            WorkOrderPayload.STREAM_CODEC,
            (payload, context) -> context.enqueueWork(() -> {
                if (context.player() instanceof ServerPlayer player) {
                    ColonySyncHandler.handleWorkOrderAction(player, payload.colonyId(), payload.action(), payload.jsonData());
                }
            })
        );

        // ---- Work order sync (server -> client) ----

        registrar.playToClient(
            WorkOrderSyncPayload.TYPE,
            WorkOrderSyncPayload.STREAM_CODEC,
            (payload, context) -> context.enqueueWork(() -> {
                WorkOrderSyncPayload.lastResponse = payload;
            })
        );

        // ---- Research actions (client -> server) ----

        registrar.playToServer(
            ResearchPayload.TYPE,
            ResearchPayload.STREAM_CODEC,
            (payload, context) -> context.enqueueWork(() -> {
                if (context.player() instanceof ServerPlayer player) {
                    ColonySyncHandler.handleResearchAction(player, payload.colonyId(), payload.action(), payload.jsonData());
                }
            })
        );

        // ---- Research sync (server -> client) ----

        registrar.playToClient(
            ResearchSyncPayload.TYPE,
            ResearchSyncPayload.STREAM_CODEC,
            (payload, context) -> context.enqueueWork(() -> {
                ResearchSyncPayload.lastResponse = payload;
            })
        );
    }
}
