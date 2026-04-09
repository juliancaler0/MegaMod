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
 * Client -> Server: uploads a rendered map tile PNG for shared storage.
 */
public record MapTileUploadPayload(
        int tileX,
        int tileZ,
        boolean cave,
        String dimension,
        long chunkBitmask,
        byte[] pngData
) implements CustomPacketPayload {

    public static final Type<MapTileUploadPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("megamod", "map_tile_upload"));

    public static final StreamCodec<FriendlyByteBuf, MapTileUploadPayload> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public MapTileUploadPayload decode(FriendlyByteBuf buf) {
                    return new MapTileUploadPayload(
                            buf.readInt(),
                            buf.readInt(),
                            buf.readBoolean(),
                            buf.readUtf(),
                            buf.readLong(),
                            buf.readByteArray(32768)
                    );
                }

                @Override
                public void encode(FriendlyByteBuf buf, MapTileUploadPayload payload) {
                    buf.writeInt(payload.tileX);
                    buf.writeInt(payload.tileZ);
                    buf.writeBoolean(payload.cave);
                    buf.writeUtf(payload.dimension);
                    buf.writeLong(payload.chunkBitmask);
                    buf.writeByteArray(payload.pngData);
                }
            };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleOnServer(MapTileUploadPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer sp) {
                ServerLevel level = (ServerLevel) sp.level();
                SharedMapManager mgr = SharedMapManager.getInstance();
                mgr.initializeIfNeeded(level);
                mgr.receiveTileUpload(sp, payload);
            }
        });
    }
}
