/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.network.codec.StreamCodec
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload$Type
 *  net.minecraft.resources.Identifier
 *  net.neoforged.neoforge.network.handling.IPayloadContext
 */
package com.ultra.megamod.feature.dungeons.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record DungeonSyncPayload(String instanceId, String tier, String theme, int roomsCleared, int totalRooms, boolean bossAlive) implements CustomPacketPayload
{
    public static volatile String clientInstanceId = "";
    public static volatile String clientTier = "";
    public static volatile String clientTheme = "";
    public static volatile int clientRoomsCleared = 0;
    public static volatile int clientTotalRooms = 0;
    public static volatile boolean clientBossAlive = false;
    public static volatile boolean clientInDungeon = false;
    public static final CustomPacketPayload.Type<DungeonSyncPayload> TYPE = new CustomPacketPayload.Type(Identifier.fromNamespaceAndPath((String)"megamod", (String)"dungeon_sync"));
    public static final StreamCodec<FriendlyByteBuf, DungeonSyncPayload> STREAM_CODEC = new StreamCodec<FriendlyByteBuf, DungeonSyncPayload>(){

        public DungeonSyncPayload decode(FriendlyByteBuf buf) {
            String instanceId = buf.readUtf();
            String tier = buf.readUtf();
            String theme = buf.readUtf();
            int roomsCleared = buf.readVarInt();
            int totalRooms = buf.readVarInt();
            boolean bossAlive = buf.readBoolean();
            return new DungeonSyncPayload(instanceId, tier, theme, roomsCleared, totalRooms, bossAlive);
        }

        public void encode(FriendlyByteBuf buf, DungeonSyncPayload payload) {
            buf.writeUtf(payload.instanceId());
            buf.writeUtf(payload.tier());
            buf.writeUtf(payload.theme());
            buf.writeVarInt(payload.roomsCleared());
            buf.writeVarInt(payload.totalRooms());
            buf.writeBoolean(payload.bossAlive());
        }
    };

    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleOnClient(DungeonSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            clientInstanceId = payload.instanceId();
            clientTier = payload.tier();
            clientTheme = payload.theme();
            clientRoomsCleared = payload.roomsCleared();
            clientTotalRooms = payload.totalRooms();
            clientBossAlive = payload.bossAlive();
            clientInDungeon = !payload.instanceId().isEmpty();
        });
    }

    public static void clearClientState() {
        clientInstanceId = "";
        clientTier = "";
        clientTheme = "";
        clientRoomsCleared = 0;
        clientTotalRooms = 0;
        clientBossAlive = false;
        clientInDungeon = false;
    }
}

