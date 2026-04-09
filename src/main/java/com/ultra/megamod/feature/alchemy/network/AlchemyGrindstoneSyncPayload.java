package com.ultra.megamod.feature.alchemy.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Server -> Client: sync grindstone state for screen rendering.
 */
public record AlchemyGrindstoneSyncPayload(
        BlockPos pos,
        boolean grinding,
        int progress,
        int total,
        boolean outputReady
) implements CustomPacketPayload {

    // Client-side cache
    public static volatile boolean clientGrinding = false;
    public static volatile int clientProgress = 0;
    public static volatile int clientTotal = 100;
    public static volatile boolean clientOutputReady = false;
    public static volatile BlockPos clientPos = BlockPos.ZERO;

    public static final CustomPacketPayload.Type<AlchemyGrindstoneSyncPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("megamod", "alchemy_grindstone_sync"));

    public static final StreamCodec<FriendlyByteBuf, AlchemyGrindstoneSyncPayload> STREAM_CODEC =
            new StreamCodec<>() {
                public AlchemyGrindstoneSyncPayload decode(FriendlyByteBuf buf) {
                    return new AlchemyGrindstoneSyncPayload(
                            buf.readBlockPos(),
                            buf.readBoolean(),
                            buf.readInt(),
                            buf.readInt(),
                            buf.readBoolean()
                    );
                }
                public void encode(FriendlyByteBuf buf, AlchemyGrindstoneSyncPayload payload) {
                    buf.writeBlockPos(payload.pos());
                    buf.writeBoolean(payload.grinding());
                    buf.writeInt(payload.progress());
                    buf.writeInt(payload.total());
                    buf.writeBoolean(payload.outputReady());
                }
            };

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleOnClient(AlchemyGrindstoneSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            clientPos = payload.pos();
            clientGrinding = payload.grinding();
            clientProgress = payload.progress();
            clientTotal = payload.total();
            clientOutputReady = payload.outputReady();
        });
    }
}
