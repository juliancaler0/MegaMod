package com.ultra.megamod.feature.citizen.request.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.UUID;

/**
 * Server-to-client payload that syncs request state for the colony request GUI.
 * Carries the essential display information for a single request.
 */
public record RequestPayload(
    UUID requestToken,
    String requestableDesc,
    String requesterName,
    int state
) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<RequestPayload> TYPE =
        new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("megamod", "request_sync"));

    public static final StreamCodec<FriendlyByteBuf, RequestPayload> STREAM_CODEC =
        StreamCodec.of(RequestPayload::write, RequestPayload::read);

    /** Client-side last response for polling in request screens. */
    public static volatile RequestPayload lastResponse = null;

    private static void write(FriendlyByteBuf buf, RequestPayload payload) {
        buf.writeUUID(payload.requestToken);
        buf.writeUtf(payload.requestableDesc, 1024);
        buf.writeUtf(payload.requesterName, 256);
        buf.writeInt(payload.state);
    }

    private static RequestPayload read(FriendlyByteBuf buf) {
        return new RequestPayload(
            buf.readUUID(),
            buf.readUtf(1024),
            buf.readUtf(256),
            buf.readInt()
        );
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
