package com.ultra.megamod.feature.casino.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Server -> Client payload for craps game state sync.
 * JSON string with: phase, die1, die2, point, betAmount, resultMessage.
 * Client polls the static {@link #lastSync} field each tick.
 */
public record CrapsGameSyncPayload(String gameStateJson) implements CustomPacketPayload {

    public static volatile CrapsGameSyncPayload lastSync = null;

    public static final CustomPacketPayload.Type<CrapsGameSyncPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("megamod", "craps_sync"));

    public static final StreamCodec<FriendlyByteBuf, CrapsGameSyncPayload> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public CrapsGameSyncPayload decode(FriendlyByteBuf buf) {
                    return new CrapsGameSyncPayload(buf.readUtf(524288));
                }

                @Override
                public void encode(FriendlyByteBuf buf, CrapsGameSyncPayload payload) {
                    buf.writeUtf(payload.gameStateJson(), 524288);
                }
            };

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleOnClient(CrapsGameSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            lastSync = payload;
        });
    }
}
