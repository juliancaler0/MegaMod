package com.ultra.megamod.feature.relics.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record OpenRelicScreenPayload(String relicName) implements CustomPacketPayload
{
    public static volatile OpenRelicScreenPayload lastPayload = null;

    public static final CustomPacketPayload.Type<OpenRelicScreenPayload> TYPE = new CustomPacketPayload.Type(Identifier.fromNamespaceAndPath((String)"megamod", (String)"open_relic_screen"));
    public static final StreamCodec<FriendlyByteBuf, OpenRelicScreenPayload> STREAM_CODEC = new StreamCodec<FriendlyByteBuf, OpenRelicScreenPayload>(){

        public OpenRelicScreenPayload decode(FriendlyByteBuf buf) {
            String relicName = buf.readUtf();
            return new OpenRelicScreenPayload(relicName);
        }

        public void encode(FriendlyByteBuf buf, OpenRelicScreenPayload payload) {
            buf.writeUtf(payload.relicName());
        }
    };

    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleOnClient(OpenRelicScreenPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> lastPayload = payload);
    }
}

