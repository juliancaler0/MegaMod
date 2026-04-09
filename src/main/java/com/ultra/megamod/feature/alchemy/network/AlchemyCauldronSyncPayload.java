package com.ultra.megamod.feature.alchemy.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Server -> Client: sync cauldron state for screen rendering.
 */
public record AlchemyCauldronSyncPayload(
        BlockPos pos,
        int waterLevel,
        String ingredients,  // comma-separated reagent IDs
        int brewingProgress,
        boolean resultReady,
        String outputPotionId
) implements CustomPacketPayload {

    // Client-side cache for screen rendering
    public static volatile int clientWater = 0;
    public static volatile String clientIngredients = "";
    public static volatile int clientBrewProgress = 0;
    public static volatile boolean clientResultReady = false;
    public static volatile String clientOutputPotion = "";
    public static volatile BlockPos clientPos = BlockPos.ZERO;

    public static final CustomPacketPayload.Type<AlchemyCauldronSyncPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("megamod", "alchemy_cauldron_sync"));

    public static final StreamCodec<FriendlyByteBuf, AlchemyCauldronSyncPayload> STREAM_CODEC =
            new StreamCodec<>() {
                public AlchemyCauldronSyncPayload decode(FriendlyByteBuf buf) {
                    return new AlchemyCauldronSyncPayload(
                            buf.readBlockPos(),
                            buf.readInt(),
                            buf.readUtf(512),
                            buf.readInt(),
                            buf.readBoolean(),
                            buf.readUtf(256)
                    );
                }
                public void encode(FriendlyByteBuf buf, AlchemyCauldronSyncPayload payload) {
                    buf.writeBlockPos(payload.pos());
                    buf.writeInt(payload.waterLevel());
                    buf.writeUtf(payload.ingredients(), 512);
                    buf.writeInt(payload.brewingProgress());
                    buf.writeBoolean(payload.resultReady());
                    buf.writeUtf(payload.outputPotionId(), 256);
                }
            };

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleOnClient(AlchemyCauldronSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            clientPos = payload.pos();
            clientWater = payload.waterLevel();
            clientIngredients = payload.ingredients();
            clientBrewProgress = payload.brewingProgress();
            clientResultReady = payload.resultReady();
            clientOutputPotion = payload.outputPotionId();
        });
    }
}
