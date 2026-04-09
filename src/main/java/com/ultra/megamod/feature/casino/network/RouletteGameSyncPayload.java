package com.ultra.megamod.feature.casino.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Server->Client: syncs the roulette game state as a JSON string.
 * Client screens poll {@link #lastSync} in their tick() method.
 */
public record RouletteGameSyncPayload(String gameStateJson) implements CustomPacketPayload {
    public static volatile RouletteGameSyncPayload lastSync = null;

    public static final CustomPacketPayload.Type<RouletteGameSyncPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("megamod", "roulette_sync"));

    public static final StreamCodec<FriendlyByteBuf, RouletteGameSyncPayload> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public RouletteGameSyncPayload decode(FriendlyByteBuf buf) {
                    return new RouletteGameSyncPayload(buf.readUtf(524288));
                }

                @Override
                public void encode(FriendlyByteBuf buf, RouletteGameSyncPayload payload) {
                    buf.writeUtf(payload.gameStateJson(), 524288);
                }
            };

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleOnClient(RouletteGameSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            lastSync = payload;
        });
    }
}
