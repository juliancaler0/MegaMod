package com.ultra.megamod.feature.arena.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Server → Client: syncs arena wave/mob status for the HUD.
 */
public record ArenaHudSyncPayload(boolean inArena, int wave, int mobsAlive) implements CustomPacketPayload {

    public static volatile boolean clientInArena = false;
    public static volatile int clientWave = 0;
    public static volatile int clientMobsAlive = 0;

    public static final Type<ArenaHudSyncPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("megamod", "arena_hud_sync"));

    public static final StreamCodec<FriendlyByteBuf, ArenaHudSyncPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public ArenaHudSyncPayload decode(FriendlyByteBuf buf) {
            return new ArenaHudSyncPayload(buf.readBoolean(), buf.readVarInt(), buf.readVarInt());
        }
        @Override
        public void encode(FriendlyByteBuf buf, ArenaHudSyncPayload p) {
            buf.writeBoolean(p.inArena());
            buf.writeVarInt(p.wave());
            buf.writeVarInt(p.mobsAlive());
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handleClient(ArenaHudSyncPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            clientInArena = payload.inArena();
            clientWave = payload.wave();
            clientMobsAlive = payload.mobsAlive();
        });
    }
}
