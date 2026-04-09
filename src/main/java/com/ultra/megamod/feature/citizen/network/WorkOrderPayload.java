package com.ultra.megamod.feature.citizen.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Client-to-server payload: work order management actions.
 *
 * <p>Supported actions:
 * <ul>
 *   <li>{@code "create"} - create a new work order. jsonData: {"buildingPos":"x,y,z","type":"BUILD","builderPos":"x,y,z"}</li>
 *   <li>{@code "cancel"} - cancel a work order. jsonData: {"orderId":"uuid"}</li>
 *   <li>{@code "change_priority"} - change priority. jsonData: {"orderId":"uuid","priority":5}</li>
 *   <li>{@code "assign_builder"} - assign/reassign builder. jsonData: {"orderId":"uuid","builderId":"uuid"}</li>
 *   <li>{@code "request"} - request full work order sync from server. jsonData: {}</li>
 * </ul>
 *
 * <p>Server responds with {@link WorkOrderSyncPayload}.
 *
 * <p>Fields:
 * <ul>
 *   <li>{@code colonyId} - the faction/colony UUID string</li>
 *   <li>{@code action} - the work order action type</li>
 *   <li>{@code jsonData} - action-specific parameters as JSON</li>
 * </ul>
 */
public record WorkOrderPayload(String colonyId, String action, String jsonData) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<WorkOrderPayload> TYPE =
        new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("megamod", "work_order"));

    public static final StreamCodec<FriendlyByteBuf, WorkOrderPayload> STREAM_CODEC =
        StreamCodec.of(WorkOrderPayload::write, WorkOrderPayload::read);

    private static void write(FriendlyByteBuf buf, WorkOrderPayload payload) {
        buf.writeUtf(payload.colonyId, 256);
        buf.writeUtf(payload.action, 256);
        buf.writeUtf(payload.jsonData, 131072);
    }

    private static WorkOrderPayload read(FriendlyByteBuf buf) {
        return new WorkOrderPayload(buf.readUtf(256), buf.readUtf(256), buf.readUtf(131072));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
