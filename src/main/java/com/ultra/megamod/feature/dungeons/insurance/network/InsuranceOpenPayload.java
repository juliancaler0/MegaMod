package com.ultra.megamod.feature.dungeons.insurance.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Server → Client: opens the insurance screen with tier info, slot costs, and party member names.
 */
public record InsuranceOpenPayload(String tierName, String jsonSlotCosts, String jsonPartyNames) implements CustomPacketPayload {

    public static volatile String clientTierName = "";
    public static volatile String clientSlotCosts = "";
    public static volatile String clientPartyNames = "";
    public static volatile boolean shouldOpenScreen = false;

    public static final CustomPacketPayload.Type<InsuranceOpenPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("megamod", "insurance_open"));

    public static final StreamCodec<FriendlyByteBuf, InsuranceOpenPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public InsuranceOpenPayload decode(FriendlyByteBuf buf) {
            return new InsuranceOpenPayload(buf.readUtf(), buf.readUtf(), buf.readUtf());
        }

        @Override
        public void encode(FriendlyByteBuf buf, InsuranceOpenPayload payload) {
            buf.writeUtf(payload.tierName());
            buf.writeUtf(payload.jsonSlotCosts());
            buf.writeUtf(payload.jsonPartyNames());
        }
    };

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleOnClient(InsuranceOpenPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            // Clear stale status from previous dungeon run before opening new screen
            InsuranceStatusPayload.clearClientState();
            clientTierName = payload.tierName();
            clientSlotCosts = payload.jsonSlotCosts();
            clientPartyNames = payload.jsonPartyNames();
            shouldOpenScreen = true;
        });
    }

    public static void clearClientState() {
        clientTierName = "";
        clientSlotCosts = "";
        clientPartyNames = "";
        shouldOpenScreen = false;
    }
}
