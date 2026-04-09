package com.ultra.megamod.feature.schematic.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Client -> Server: sends schematic data + placement info for build order creation.
 * Contains the raw .litematic file bytes (compressed) and placement parameters.
 * If builderEntityId >= 0, the build order is auto-assigned to that builder.
 */
public record SchematicPlacementPayload(
        byte[] schematicData,
        String fileName,
        BlockPos origin,
        int rotation,
        int mirror,
        int builderEntityId
) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SchematicPlacementPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("megamod", "schematic_placement"));

    public static final StreamCodec<FriendlyByteBuf, SchematicPlacementPayload> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public SchematicPlacementPayload decode(FriendlyByteBuf buf) {
                    int dataLen = buf.readVarInt();
                    byte[] data = buf.readByteArray(dataLen > 0 ? dataLen : 5242880); // 5MB max
                    String fileName = buf.readUtf(256);
                    BlockPos origin = buf.readBlockPos();
                    int rotation = buf.readByte();
                    int mirror = buf.readByte();
                    int builderId = buf.readInt();
                    return new SchematicPlacementPayload(data, fileName, origin, rotation, mirror, builderId);
                }

                @Override
                public void encode(FriendlyByteBuf buf, SchematicPlacementPayload payload) {
                    buf.writeVarInt(payload.schematicData().length);
                    buf.writeByteArray(payload.schematicData());
                    buf.writeUtf(payload.fileName(), 256);
                    buf.writeBlockPos(payload.origin());
                    buf.writeByte(payload.rotation());
                    buf.writeByte(payload.mirror());
                    buf.writeInt(payload.builderEntityId());
                }
            };

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleOnServer(SchematicPlacementPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                SchematicServerHandler.handlePlacement(serverPlayer, payload);
            }
        });
    }
}
