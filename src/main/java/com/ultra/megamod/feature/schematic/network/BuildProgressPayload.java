package com.ultra.megamod.feature.schematic.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Server -> Client: syncs build order progress and material status.
 */
public record BuildProgressPayload(
        String orderId,
        int progress,
        int total,
        String status,
        String missingItems
) implements CustomPacketPayload {

    /** Latest response from server, polled by screens in tick(). */
    public static volatile BuildProgressPayload lastResponse = null;

    public static final CustomPacketPayload.Type<BuildProgressPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("megamod", "build_progress"));

    public static final StreamCodec<FriendlyByteBuf, BuildProgressPayload> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public BuildProgressPayload decode(FriendlyByteBuf buf) {
                    return new BuildProgressPayload(
                            buf.readUtf(64), buf.readVarInt(), buf.readVarInt(),
                            buf.readUtf(32), buf.readUtf(4096));
                }

                @Override
                public void encode(FriendlyByteBuf buf, BuildProgressPayload payload) {
                    buf.writeUtf(payload.orderId(), 64);
                    buf.writeVarInt(payload.progress());
                    buf.writeVarInt(payload.total());
                    buf.writeUtf(payload.status(), 32);
                    buf.writeUtf(payload.missingItems(), 4096);
                }
            };

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleOnClient(BuildProgressPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> lastResponse = payload);
    }
}
