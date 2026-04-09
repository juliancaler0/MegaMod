package com.ultra.megamod.feature.casino.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record BlackjackSyncPayload(String gameStateJson) implements CustomPacketPayload {
    public static volatile BlackjackSyncPayload lastSync = null;

    public static final CustomPacketPayload.Type<BlackjackSyncPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("megamod", "blackjack_sync"));

    public static final StreamCodec<FriendlyByteBuf, BlackjackSyncPayload> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public BlackjackSyncPayload decode(FriendlyByteBuf buf) {
                    return new BlackjackSyncPayload(buf.readUtf(524288));
                }

                @Override
                public void encode(FriendlyByteBuf buf, BlackjackSyncPayload payload) {
                    buf.writeUtf(payload.gameStateJson(), 524288);
                }
            };

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleOnClient(BlackjackSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            lastSync = payload;
            com.ultra.megamod.feature.casino.blackjack.BlackjackTableBlockEntity.clientGameState = payload.gameStateJson();
        });
    }
}
