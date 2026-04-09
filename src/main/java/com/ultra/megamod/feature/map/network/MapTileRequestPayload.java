package com.ultra.megamod.feature.map.network;

import com.ultra.megamod.feature.map.SharedMapManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Client -> Server: requests one or more map tiles by coordinates.
 * Coordinates are flattened pairs: [x1, z1, x2, z2, ...], max 16 tiles (32 ints).
 */
public record MapTileRequestPayload(
        boolean cave,
        String dimension,
        int[] tileCoords
) implements CustomPacketPayload {

    public static final Type<MapTileRequestPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("megamod", "map_tile_request"));

    public static final StreamCodec<FriendlyByteBuf, MapTileRequestPayload> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public MapTileRequestPayload decode(FriendlyByteBuf buf) {
                    boolean cave = buf.readBoolean();
                    String dimension = buf.readUtf();
                    int len = buf.readVarInt();
                    if (len > 32) len = 32;
                    int[] coords = new int[len];
                    for (int i = 0; i < len; i++) {
                        coords[i] = buf.readInt();
                    }
                    return new MapTileRequestPayload(cave, dimension, coords);
                }

                @Override
                public void encode(FriendlyByteBuf buf, MapTileRequestPayload payload) {
                    buf.writeBoolean(payload.cave);
                    buf.writeUtf(payload.dimension);
                    int len = Math.min(payload.tileCoords.length, 32);
                    buf.writeVarInt(len);
                    for (int i = 0; i < len; i++) {
                        buf.writeInt(payload.tileCoords[i]);
                    }
                }
            };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleOnServer(MapTileRequestPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer sp) {
                ServerLevel level = (ServerLevel) sp.level();
                SharedMapManager mgr = SharedMapManager.getInstance();
                mgr.initializeIfNeeded(level);
                mgr.handleTileRequest(sp, payload);
            }
        });
    }
}
