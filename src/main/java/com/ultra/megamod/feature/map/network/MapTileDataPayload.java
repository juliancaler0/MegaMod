package com.ultra.megamod.feature.map.network;

import com.ultra.megamod.feature.map.SharedMapTileReceiver;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Server -> Client: sends a map tile's PNG data.
 * Empty pngData signals the server has no data for that tile.
 */
public record MapTileDataPayload(
        int tileX,
        int tileZ,
        boolean cave,
        String dimension,
        long chunkBitmask,
        byte[] pngData
) implements CustomPacketPayload {

    public static final Type<MapTileDataPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("megamod", "map_tile_data"));

    public static final StreamCodec<FriendlyByteBuf, MapTileDataPayload> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public MapTileDataPayload decode(FriendlyByteBuf buf) {
                    return new MapTileDataPayload(
                            buf.readInt(),
                            buf.readInt(),
                            buf.readBoolean(),
                            buf.readUtf(),
                            buf.readLong(),
                            buf.readByteArray(32768)
                    );
                }

                @Override
                public void encode(FriendlyByteBuf buf, MapTileDataPayload payload) {
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

    public static void handleOnClient(MapTileDataPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> SharedMapTileReceiver.receiveTileData(payload));
    }
}
