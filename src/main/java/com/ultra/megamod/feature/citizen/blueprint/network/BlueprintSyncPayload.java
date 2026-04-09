package com.ultra.megamod.feature.citizen.blueprint.network;

import com.mojang.logging.LogUtils;
import com.ultra.megamod.feature.citizen.blueprint.Blueprint;
import com.ultra.megamod.feature.citizen.blueprint.BlueprintUtil;
import com.ultra.megamod.feature.citizen.blueprint.RotationMirror;
import com.ultra.megamod.feature.citizen.blueprint.client.BlueprintRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.slf4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * Server -> Client: sync blueprint data for rendering preview.
 * Contains compressed NBT blueprint data, position, and rotation/mirror ordinal.
 *
 * <p>The blueprint data is in the format produced by {@link BlueprintUtil#writeBlueprintToNBT(Blueprint)}.
 */
public record BlueprintSyncPayload(
        byte[] blueprintData,
        BlockPos position,
        int rotationMirrorOrdinal
) implements CustomPacketPayload {

    private static final Logger LOGGER = LogUtils.getLogger();

    public static final CustomPacketPayload.Type<BlueprintSyncPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("megamod", "blueprint_sync"));

    public static final StreamCodec<FriendlyByteBuf, BlueprintSyncPayload> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public BlueprintSyncPayload decode(FriendlyByteBuf buf) {
                    byte[] data = buf.readByteArray(5242880); // 5MB max
                    BlockPos pos = buf.readBlockPos();
                    int rmOrdinal = buf.readByte();
                    return new BlueprintSyncPayload(data, pos, rmOrdinal);
                }

                @Override
                public void encode(FriendlyByteBuf buf, BlueprintSyncPayload payload) {
                    buf.writeByteArray(payload.blueprintData());
                    buf.writeBlockPos(payload.position());
                    buf.writeByte(payload.rotationMirrorOrdinal());
                }
            };

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * Handles this payload on the client: decompress the blueprint NBT,
     * parse it using BlueprintUtil, and set the BlueprintRenderer preview.
     */
    public static void handleOnClient(BlueprintSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            try {
                // Decompress the blueprint NBT data
                ByteArrayInputStream bais = new ByteArrayInputStream(payload.blueprintData());
                CompoundTag root = NbtIo.readCompressed(bais, NbtAccounter.unlimitedHeap());

                // Parse using BlueprintUtil for proper format handling
                Blueprint blueprint = BlueprintUtil.readBlueprintFromNBT(root);
                if (blueprint == null) {
                    LOGGER.warn("Failed to parse blueprint from sync payload");
                    return;
                }

                // Determine rotation/mirror from ordinal
                RotationMirror rm = RotationMirror.NONE;
                RotationMirror[] values = RotationMirror.values();
                if (payload.rotationMirrorOrdinal() >= 0 && payload.rotationMirrorOrdinal() < values.length) {
                    rm = values[payload.rotationMirrorOrdinal()];
                }

                // Set the renderer preview using the blueprint's block info list
                BlueprintRenderer.setPreview(blueprint.getBlockInfoAsList(), payload.position(), rm);
            } catch (Exception e) {
                LOGGER.warn("Failed to handle blueprint sync: {}", e.getMessage());
            }
        });
    }

    /**
     * Utility: compress a blueprint into bytes suitable for sending in this payload.
     *
     * @param blueprint the blueprint to compress
     * @return compressed byte array of the blueprint's NBT
     */
    public static byte[] compressBlueprint(Blueprint blueprint) {
        try {
            CompoundTag tag = BlueprintUtil.writeBlueprintToNBT(blueprint);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            NbtIo.writeCompressed(tag, baos);
            return baos.toByteArray();
        } catch (Exception e) {
            LOGGER.error("Failed to compress blueprint: {}", e.getMessage());
            return new byte[0];
        }
    }

    /**
     * Utility: compress a raw CompoundTag into bytes suitable for sending in this payload.
     *
     * @param tag the blueprint root NBT tag
     * @return compressed byte array
     */
    public static byte[] compressBlueprintTag(CompoundTag tag) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            NbtIo.writeCompressed(tag, baos);
            return baos.toByteArray();
        } catch (Exception e) {
            LOGGER.error("Failed to compress blueprint tag: {}", e.getMessage());
            return new byte[0];
        }
    }
}
