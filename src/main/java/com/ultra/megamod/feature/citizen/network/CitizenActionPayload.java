package com.ultra.megamod.feature.citizen.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record CitizenActionPayload(int entityId, String action, String jsonData) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<CitizenActionPayload> TYPE =
        new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("megamod", "citizen_action"));

    public static final StreamCodec<FriendlyByteBuf, CitizenActionPayload> STREAM_CODEC =
        StreamCodec.of(CitizenActionPayload::write, CitizenActionPayload::read);

    private static void write(FriendlyByteBuf buf, CitizenActionPayload payload) {
        buf.writeInt(payload.entityId);
        buf.writeUtf(payload.action, 256);
        buf.writeUtf(payload.jsonData, 32767);
    }

    private static CitizenActionPayload read(FriendlyByteBuf buf) {
        return new CitizenActionPayload(buf.readInt(), buf.readUtf(256), buf.readUtf(32767));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
