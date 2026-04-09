package com.ultra.megamod.feature.citizen.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Server-to-client payload: building-specific data sync.
 * Sent when a player opens a building's GUI (hut block interaction).
 *
 * <p>Fields:
 * <ul>
 *   <li>{@code colonyId} - the faction/colony UUID string</li>
 *   <li>{@code buildingPos} - packed BlockPos as long (BlockPos.asLong)</li>
 *   <li>{@code jsonData} - JSON object with building state:
 *       buildingId, displayName, level, style, isBuilt, needsRepair,
 *       assignedWorkers[], recipes[], settings{}, modules[],
 *       workOrderId (if any), customName</li>
 * </ul>
 */
public record BuildingViewPayload(String colonyId, long buildingPos, String jsonData) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<BuildingViewPayload> TYPE =
        new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("megamod", "building_view"));

    public static final StreamCodec<FriendlyByteBuf, BuildingViewPayload> STREAM_CODEC =
        StreamCodec.of(BuildingViewPayload::write, BuildingViewPayload::read);

    /** Client-side last received building view for screen polling. */
    public static volatile BuildingViewPayload lastResponse = null;

    private static void write(FriendlyByteBuf buf, BuildingViewPayload payload) {
        buf.writeUtf(payload.colonyId, 256);
        buf.writeLong(payload.buildingPos);
        buf.writeUtf(payload.jsonData, 131072);
    }

    private static BuildingViewPayload read(FriendlyByteBuf buf) {
        return new BuildingViewPayload(buf.readUtf(256), buf.readLong(), buf.readUtf(131072));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
