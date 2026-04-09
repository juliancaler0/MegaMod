package com.ultra.megamod.feature.citizen.request.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.UUID;

/**
 * Client-to-server payload sent when a player manually fulfills a request.
 * The server validates that the player has the matching item in their inventory,
 * removes it, and completes the request.
 */
public record RequestFulfillPayload(UUID requestToken) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<RequestFulfillPayload> TYPE =
        new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("megamod", "request_fulfill"));

    public static final StreamCodec<FriendlyByteBuf, RequestFulfillPayload> STREAM_CODEC =
        StreamCodec.of(RequestFulfillPayload::write, RequestFulfillPayload::read);

    private static void write(FriendlyByteBuf buf, RequestFulfillPayload payload) {
        buf.writeUUID(payload.requestToken);
    }

    private static RequestFulfillPayload read(FriendlyByteBuf buf) {
        return new RequestFulfillPayload(buf.readUUID());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
