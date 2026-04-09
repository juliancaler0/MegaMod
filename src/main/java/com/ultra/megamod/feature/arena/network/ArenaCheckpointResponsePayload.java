package com.ultra.megamod.feature.arena.network;

import com.ultra.megamod.feature.arena.ArenaManager;
import com.ultra.megamod.feature.dimensions.MegaModDimensions;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Client → Server: player chose to continue or leave at a checkpoint.
 */
public record ArenaCheckpointResponsePayload(boolean continueArena) implements CustomPacketPayload {

    public static final Type<ArenaCheckpointResponsePayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("megamod", "arena_checkpoint_response"));

    public static final StreamCodec<FriendlyByteBuf, ArenaCheckpointResponsePayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public ArenaCheckpointResponsePayload decode(FriendlyByteBuf buf) {
            return new ArenaCheckpointResponsePayload(buf.readBoolean());
        }
        @Override
        public void encode(FriendlyByteBuf buf, ArenaCheckpointResponsePayload payload) {
            buf.writeBoolean(payload.continueArena());
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handleServer(ArenaCheckpointResponsePayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;
            ServerLevel overworld = player.level().getServer().overworld();
            ArenaManager arenaManager = ArenaManager.get(overworld);
            ArenaManager.ArenaInstance instance = arenaManager.getInstanceForPlayer(player.getUUID());
            if (instance == null || !instance.atCheckpoint) return;

            ServerLevel pocketLevel = player.level().getServer().getLevel(MegaModDimensions.DUNGEON);
            if (pocketLevel == null) return;

            if (payload.continueArena()) {
                arenaManager.continueFromCheckpoint(instance.instanceId, pocketLevel);
            } else {
                arenaManager.leaveAtCheckpoint(instance.instanceId, pocketLevel);
            }
        });
    }
}
