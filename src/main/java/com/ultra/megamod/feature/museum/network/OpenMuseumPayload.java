package com.ultra.megamod.feature.museum.network;

import com.ultra.megamod.MegaMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record OpenMuseumPayload(String museumJson) implements CustomPacketPayload
{
    public static volatile OpenMuseumPayload lastPayload = null;

    public static final CustomPacketPayload.Type<OpenMuseumPayload> TYPE = new CustomPacketPayload.Type(Identifier.fromNamespaceAndPath((String)"megamod", (String)"open_museum"));
    public static final StreamCodec<FriendlyByteBuf, OpenMuseumPayload> STREAM_CODEC = new StreamCodec<FriendlyByteBuf, OpenMuseumPayload>(){

        public OpenMuseumPayload decode(FriendlyByteBuf buf) {
            return new OpenMuseumPayload(buf.readUtf(Short.MAX_VALUE));
        }

        public void encode(FriendlyByteBuf buf, OpenMuseumPayload payload) {
            buf.writeUtf(payload.museumJson(), Short.MAX_VALUE);
        }
    };

    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleOnClient(OpenMuseumPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            MegaMod.LOGGER.info("Received museum data on client: {} chars", (Object)payload.museumJson().length());
            lastPayload = payload;
        });
    }
}

