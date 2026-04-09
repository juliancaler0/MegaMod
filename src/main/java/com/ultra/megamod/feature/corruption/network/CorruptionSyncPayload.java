package com.ultra.megamod.feature.corruption.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Server-to-client payload that syncs the corruption strength at the player's current location.
 * Sent periodically from CorruptionEvents.onPlayerTick when the player is in a corrupted chunk.
 */
public record CorruptionSyncPayload(int strength) implements CustomPacketPayload {
    public static volatile int clientCorruptionStrength = -1;

    public static final CustomPacketPayload.Type<CorruptionSyncPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("megamod", "corruption_sync"));

    public static final StreamCodec<FriendlyByteBuf, CorruptionSyncPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public CorruptionSyncPayload decode(FriendlyByteBuf buf) {
            return new CorruptionSyncPayload(buf.readVarInt());
        }

        @Override
        public void encode(FriendlyByteBuf buf, CorruptionSyncPayload p) {
            buf.writeVarInt(p.strength());
        }
    };

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleOnClient(CorruptionSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> clientCorruptionStrength = payload.strength());
    }
}
