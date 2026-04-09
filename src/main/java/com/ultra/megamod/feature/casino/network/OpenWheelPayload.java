package com.ultra.megamod.feature.casino.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record OpenWheelPayload(BlockPos pos) implements CustomPacketPayload {
    public static volatile OpenWheelPayload lastPayload = null;

    public static final CustomPacketPayload.Type<OpenWheelPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("megamod", "open_wheel"));

    public static final StreamCodec<FriendlyByteBuf, OpenWheelPayload> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public OpenWheelPayload decode(FriendlyByteBuf buf) {
                    return new OpenWheelPayload(buf.readBlockPos());
                }

                @Override
                public void encode(FriendlyByteBuf buf, OpenWheelPayload payload) {
                    buf.writeBlockPos(payload.pos());
                }
            };

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleOnClient(OpenWheelPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> lastPayload = payload);
    }
}
