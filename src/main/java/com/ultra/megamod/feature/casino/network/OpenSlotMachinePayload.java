package com.ultra.megamod.feature.casino.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record OpenSlotMachinePayload(BlockPos pos, int betIndex, int lineMode, int wallet) implements CustomPacketPayload {
    public static volatile OpenSlotMachinePayload lastPayload = null;

    public static final CustomPacketPayload.Type<OpenSlotMachinePayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("megamod", "open_slot_machine"));

    public static final StreamCodec<FriendlyByteBuf, OpenSlotMachinePayload> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public OpenSlotMachinePayload decode(FriendlyByteBuf buf) {
                    return new OpenSlotMachinePayload(buf.readBlockPos(), buf.readInt(), buf.readInt(), buf.readInt());
                }

                @Override
                public void encode(FriendlyByteBuf buf, OpenSlotMachinePayload payload) {
                    buf.writeBlockPos(payload.pos());
                    buf.writeInt(payload.betIndex());
                    buf.writeInt(payload.lineMode());
                    buf.writeInt(payload.wallet());
                }
            };

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleOnClient(OpenSlotMachinePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> lastPayload = payload);
    }
}
