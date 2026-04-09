package com.ultra.megamod.feature.computer.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record OpenComputerPayload(boolean isAdmin, int wallet, int bank) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<OpenComputerPayload> TYPE =
            new CustomPacketPayload.Type(Identifier.fromNamespaceAndPath("megamod", "open_computer"));
    public static final StreamCodec<FriendlyByteBuf, OpenComputerPayload> STREAM_CODEC = new StreamCodec<>() {
        public OpenComputerPayload decode(FriendlyByteBuf buf) {
            return new OpenComputerPayload(buf.readBoolean(), buf.readInt(), buf.readInt());
        }
        public void encode(FriendlyByteBuf buf, OpenComputerPayload payload) {
            buf.writeBoolean(payload.isAdmin());
            buf.writeInt(payload.wallet());
            buf.writeInt(payload.bank());
        }
    };

    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }

    // Stored for client-side pickup — no Screen import needed
    public static volatile OpenComputerPayload lastPayload = null;

    public static void handleOnClient(OpenComputerPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> lastPayload = payload);
    }
}
