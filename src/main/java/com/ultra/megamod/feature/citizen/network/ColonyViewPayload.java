package com.ultra.megamod.feature.citizen.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Server-to-client payload: full colony state sync.
 * Sent when a player opens the Town Hall GUI or on periodic tick updates.
 *
 * <p>Carries the colony's core data serialized as JSON so that the client-side
 * colony view can reconstruct the full state (citizens list, buildings list,
 * permissions, happiness, research progress, etc.).
 *
 * <p>Fields:
 * <ul>
 *   <li>{@code colonyId} - the faction/colony UUID string</li>
 *   <li>{@code isFullSync} - true for initial subscription, false for delta updates</li>
 *   <li>{@code jsonData} - JSON object with colony state:
 *       name, ownerId, citizens[], buildings[], permissions{}, happiness,
 *       researchProgress{}, settings{}, statistics{}</li>
 * </ul>
 */
public record ColonyViewPayload(String colonyId, boolean isFullSync, String jsonData) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ColonyViewPayload> TYPE =
        new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("megamod", "colony_view"));

    public static final StreamCodec<FriendlyByteBuf, ColonyViewPayload> STREAM_CODEC =
        StreamCodec.of(ColonyViewPayload::write, ColonyViewPayload::read);

    /** Client-side last received colony view for screen polling. */
    public static volatile ColonyViewPayload lastResponse = null;

    private static void write(FriendlyByteBuf buf, ColonyViewPayload payload) {
        buf.writeUtf(payload.colonyId, 256);
        buf.writeBoolean(payload.isFullSync);
        buf.writeUtf(payload.jsonData, 262144);
    }

    private static ColonyViewPayload read(FriendlyByteBuf buf) {
        return new ColonyViewPayload(buf.readUtf(256), buf.readBoolean(), buf.readUtf(262144));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
