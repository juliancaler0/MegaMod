package com.ultra.megamod.feature.computer.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ComputerDataPayload(String dataType, String jsonData, int wallet, int bank) implements CustomPacketPayload {
    public static volatile ComputerDataPayload lastResponse = null;
    public static final CustomPacketPayload.Type<ComputerDataPayload> TYPE =
            new CustomPacketPayload.Type(Identifier.fromNamespaceAndPath("megamod", "computer_data"));
    public static final StreamCodec<FriendlyByteBuf, ComputerDataPayload> STREAM_CODEC = new StreamCodec<>() {
        public ComputerDataPayload decode(FriendlyByteBuf buf) {
            return new ComputerDataPayload(buf.readUtf(32767), buf.readUtf(524288), buf.readInt(), buf.readInt());
        }
        public void encode(FriendlyByteBuf buf, ComputerDataPayload payload) {
            buf.writeUtf(payload.dataType(), 32767);
            buf.writeUtf(payload.jsonData(), 524288);
            buf.writeInt(payload.wallet());
            buf.writeInt(payload.bank());
        }
    };

    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handleOnClient(ComputerDataPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> lastResponse = payload);
    }
}
