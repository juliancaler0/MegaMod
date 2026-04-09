package com.ultra.megamod.feature.citizen.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record CitizenDataPayload(String dataType, String jsonData) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<CitizenDataPayload> TYPE =
        new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("megamod", "citizen_data"));

    public static final StreamCodec<FriendlyByteBuf, CitizenDataPayload> STREAM_CODEC =
        StreamCodec.of(CitizenDataPayload::write, CitizenDataPayload::read);

    // Client-side last response for polling
    public static volatile CitizenDataPayload lastResponse = null;

    private static void write(FriendlyByteBuf buf, CitizenDataPayload payload) {
        buf.writeUtf(payload.dataType, 256);
        buf.writeUtf(payload.jsonData, 65535);
    }

    private static CitizenDataPayload read(FriendlyByteBuf buf) {
        return new CitizenDataPayload(buf.readUtf(256), buf.readUtf(65535));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
