package com.ultra.megamod.feature.dungeons.insurance.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Server → Client: broadcasts party ready status updates to all session members.
 */
public record InsuranceStatusPayload(String jsonReadyStatus, boolean allReady, boolean cancelled) implements CustomPacketPayload {

    public static volatile String clientReadyStatus = "{}";
    public static volatile boolean clientAllReady = false;
    public static volatile boolean clientCancelled = false;

    public static final CustomPacketPayload.Type<InsuranceStatusPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("megamod", "insurance_status"));

    public static final StreamCodec<FriendlyByteBuf, InsuranceStatusPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public InsuranceStatusPayload decode(FriendlyByteBuf buf) {
            return new InsuranceStatusPayload(buf.readUtf(), buf.readBoolean(), buf.readBoolean());
        }

        @Override
        public void encode(FriendlyByteBuf buf, InsuranceStatusPayload payload) {
            buf.writeUtf(payload.jsonReadyStatus());
            buf.writeBoolean(payload.allReady());
            buf.writeBoolean(payload.cancelled());
        }
    };

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleOnClient(InsuranceStatusPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            clientReadyStatus = payload.jsonReadyStatus();
            clientAllReady = payload.allReady();
            clientCancelled = payload.cancelled();
        });
    }

    public static void clearClientState() {
        clientReadyStatus = "{}";
        clientAllReady = false;
        clientCancelled = false;
    }
}
