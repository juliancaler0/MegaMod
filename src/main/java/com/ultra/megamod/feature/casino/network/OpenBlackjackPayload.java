package com.ultra.megamod.feature.casino.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record OpenBlackjackPayload(BlockPos tablePos) implements CustomPacketPayload {
    public static volatile OpenBlackjackPayload lastPayload = null;

    public static final CustomPacketPayload.Type<OpenBlackjackPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("megamod", "open_blackjack"));

    public static final StreamCodec<FriendlyByteBuf, OpenBlackjackPayload> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public OpenBlackjackPayload decode(FriendlyByteBuf buf) {
                    return new OpenBlackjackPayload(buf.readBlockPos());
                }

                @Override
                public void encode(FriendlyByteBuf buf, OpenBlackjackPayload payload) {
                    buf.writeBlockPos(payload.tablePos());
                }
            };

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleOnClient(OpenBlackjackPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> lastPayload = payload);
    }
}
