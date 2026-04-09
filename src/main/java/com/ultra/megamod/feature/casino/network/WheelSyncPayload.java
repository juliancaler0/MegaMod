package com.ultra.megamod.feature.casino.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record WheelSyncPayload(String wheelStateJson) implements CustomPacketPayload {
    public static volatile WheelSyncPayload lastSync = null;

    public static final CustomPacketPayload.Type<WheelSyncPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("megamod", "wheel_sync"));

    public static final StreamCodec<FriendlyByteBuf, WheelSyncPayload> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public WheelSyncPayload decode(FriendlyByteBuf buf) {
                    return new WheelSyncPayload(buf.readUtf(524288));
                }

                @Override
                public void encode(FriendlyByteBuf buf, WheelSyncPayload payload) {
                    buf.writeUtf(payload.wheelStateJson(), 524288);
                }
            };

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleOnClient(WheelSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            lastSync = payload;
            // Update client-side rendering state for BER
            try {
                com.google.gson.JsonObject json = com.google.gson.JsonParser.parseString(payload.wheelStateJson()).getAsJsonObject();
                com.ultra.megamod.feature.casino.wheel.WheelBlockEntity.clientPhase = json.has("phase") ? json.get("phase").getAsString() : "BETTING";
                com.ultra.megamod.feature.casino.wheel.WheelBlockEntity.clientSpinAngle = json.has("spinAngle") ? json.get("spinAngle").getAsFloat() : 0f;
                com.ultra.megamod.feature.casino.wheel.WheelBlockEntity.clientTimer = json.has("timer") ? json.get("timer").getAsInt() : 0;
                if (json.has("result")) {
                    try {
                        com.ultra.megamod.feature.casino.wheel.WheelBlockEntity.clientResult = com.ultra.megamod.feature.casino.wheel.WheelSegment.valueOf(json.get("result").getAsString()).ordinal();
                    } catch (Exception ignored) {}
                } else {
                    com.ultra.megamod.feature.casino.wheel.WheelBlockEntity.clientResult = -1;
                }
            } catch (Exception ignored) {}
        });
    }
}
