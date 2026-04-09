package com.ultra.megamod.feature.citizen.multipiston;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Client-to-server payload for configuring a Multi-Piston block.
 */
public record MultiPistonPayload(
    BlockPos pos,
    int inputDir,
    int outputDir,
    int range,
    int speed
) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<MultiPistonPayload> TYPE =
        new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("megamod", "multi_piston_config"));

    public static final StreamCodec<FriendlyByteBuf, MultiPistonPayload> STREAM_CODEC =
        StreamCodec.of(MultiPistonPayload::write, MultiPistonPayload::read);

    private static void write(FriendlyByteBuf buf, MultiPistonPayload payload) {
        buf.writeBlockPos(payload.pos);
        buf.writeInt(payload.inputDir);
        buf.writeInt(payload.outputDir);
        buf.writeInt(payload.range);
        buf.writeInt(payload.speed);
    }

    private static MultiPistonPayload read(FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        int inputDir = buf.readInt();
        int outputDir = buf.readInt();
        int range = buf.readInt();
        int speed = buf.readInt();
        return new MultiPistonPayload(pos, inputDir, outputDir, range, speed);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
