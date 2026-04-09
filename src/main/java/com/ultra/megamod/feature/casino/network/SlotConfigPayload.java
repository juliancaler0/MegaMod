package com.ultra.megamod.feature.casino.network;

import com.ultra.megamod.feature.casino.slots.SlotMachineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SlotConfigPayload(BlockPos pos, int betIndex, int lineMode) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SlotConfigPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("megamod", "slot_config"));

    public static final StreamCodec<FriendlyByteBuf, SlotConfigPayload> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public SlotConfigPayload decode(FriendlyByteBuf buf) {
                    BlockPos pos = buf.readBlockPos();
                    int betIndex = buf.readInt();
                    int lineMode = buf.readInt();
                    return new SlotConfigPayload(pos, betIndex, lineMode);
                }

                @Override
                public void encode(FriendlyByteBuf buf, SlotConfigPayload payload) {
                    buf.writeBlockPos(payload.pos());
                    buf.writeInt(payload.betIndex());
                    buf.writeInt(payload.lineMode());
                }
            };

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleOnServer(SlotConfigPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) {
                return;
            }
            ServerLevel level = (ServerLevel) serverPlayer.level();
            BlockEntity be = level.getBlockEntity(payload.pos());
            if (!(be instanceof SlotMachineBlockEntity slotBE)) {
                return;
            }

            // Validate the player is the occupant
            if (!slotBE.isUsedBy(serverPlayer.getUUID())) {
                return;
            }

            slotBE.setBetIndex(payload.betIndex());
            slotBE.setLineMode(payload.lineMode());
        });
    }
}
