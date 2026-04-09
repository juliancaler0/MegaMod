package com.ultra.megamod.feature.citizen.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Server-to-client payload: work order state sync.
 * Sent in response to {@link WorkOrderPayload} requests or when work orders change.
 *
 * <p>Actions:
 * <ul>
 *   <li>{@code "sync"} - full work order list. jsonData: {"orders":[{orderId, buildingId, type, targetLevel, priority, claimed, position, assignedBuilderId}]}</li>
 *   <li>{@code "update"} - single work order update. jsonData: single work order JSON</li>
 *   <li>{@code "remove"} - a work order was removed. jsonData: {"orderId":"uuid"}</li>
 * </ul>
 *
 * <p>Fields:
 * <ul>
 *   <li>{@code colonyId} - the faction/colony UUID string</li>
 *   <li>{@code action} - the sync action type</li>
 *   <li>{@code jsonData} - work order data as JSON</li>
 * </ul>
 */
public record WorkOrderSyncPayload(String colonyId, String action, String jsonData) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<WorkOrderSyncPayload> TYPE =
        new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("megamod", "work_order_sync"));

    public static final StreamCodec<FriendlyByteBuf, WorkOrderSyncPayload> STREAM_CODEC =
        StreamCodec.of(WorkOrderSyncPayload::write, WorkOrderSyncPayload::read);

    /** Client-side last received work order sync for screen polling. */
    public static volatile WorkOrderSyncPayload lastResponse = null;

    private static void write(FriendlyByteBuf buf, WorkOrderSyncPayload payload) {
        buf.writeUtf(payload.colonyId, 256);
        buf.writeUtf(payload.action, 256);
        buf.writeUtf(payload.jsonData, 131072);
    }

    private static WorkOrderSyncPayload read(FriendlyByteBuf buf) {
        return new WorkOrderSyncPayload(buf.readUtf(256), buf.readUtf(256), buf.readUtf(131072));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
