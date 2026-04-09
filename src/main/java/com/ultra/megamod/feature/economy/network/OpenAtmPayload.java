package com.ultra.megamod.feature.economy.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record OpenAtmPayload(int wallet, int bank) implements CustomPacketPayload {
    public static volatile OpenAtmPayload lastPayload = null;

    public static final CustomPacketPayload.Type<OpenAtmPayload> TYPE = new CustomPacketPayload.Type(Identifier.fromNamespaceAndPath("megamod", "open_atm"));
    public static final StreamCodec<FriendlyByteBuf, OpenAtmPayload> STREAM_CODEC = new StreamCodec<FriendlyByteBuf, OpenAtmPayload>() {
        public OpenAtmPayload decode(FriendlyByteBuf buf) {
            int wallet = buf.readInt();
            int bank = buf.readInt();
            return new OpenAtmPayload(wallet, bank);
        }

        public void encode(FriendlyByteBuf buf, OpenAtmPayload payload) {
            buf.writeInt(payload.wallet());
            buf.writeInt(payload.bank());
        }
    };

    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleOnClient(OpenAtmPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> lastPayload = payload);
    }
}
