package com.ultra.megamod.feature.arena.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Server → Client: opens the arena checkpoint screen.
 */
public record ArenaCheckpointPayload(int wave, int reward) implements CustomPacketPayload {

    public static final Type<ArenaCheckpointPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("megamod", "arena_checkpoint"));

    public static final StreamCodec<FriendlyByteBuf, ArenaCheckpointPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public ArenaCheckpointPayload decode(FriendlyByteBuf buf) {
            return new ArenaCheckpointPayload(buf.readVarInt(), buf.readVarInt());
        }
        @Override
        public void encode(FriendlyByteBuf buf, ArenaCheckpointPayload payload) {
            buf.writeVarInt(payload.wave());
            buf.writeVarInt(payload.reward());
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    // Client-side state for screen opening
    public static volatile boolean shouldOpen = false;
    public static volatile int clientWave = 0;
    public static volatile int clientReward = 0;

    public static void handleClient(ArenaCheckpointPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            clientWave = payload.wave();
            clientReward = payload.reward();
            shouldOpen = true;
        });
    }

    public static void clearClientState() {
        shouldOpen = false;
        clientWave = 0;
        clientReward = 0;
    }
}
