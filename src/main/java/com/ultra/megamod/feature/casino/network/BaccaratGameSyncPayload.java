package com.ultra.megamod.feature.casino.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Server -> Client payload for baccarat game state sync.
 * JSON fields: phase, playerCards, bankerCards, playerValue, bankerValue,
 *              betSide, betAmount, result, resultMessage, payout
 */
public record BaccaratGameSyncPayload(String gameStateJson) implements CustomPacketPayload {
    public static volatile BaccaratGameSyncPayload lastSync = null;

    public static final CustomPacketPayload.Type<BaccaratGameSyncPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("megamod", "baccarat_sync"));

    public static final StreamCodec<FriendlyByteBuf, BaccaratGameSyncPayload> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public BaccaratGameSyncPayload decode(FriendlyByteBuf buf) {
                    return new BaccaratGameSyncPayload(buf.readUtf(524288));
                }

                @Override
                public void encode(FriendlyByteBuf buf, BaccaratGameSyncPayload payload) {
                    buf.writeUtf(payload.gameStateJson(), 524288);
                }
            };

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleOnClient(BaccaratGameSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            lastSync = payload;
        });
    }
}
