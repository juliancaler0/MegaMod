package com.ultra.megamod.feature.casino.network;

import com.ultra.megamod.feature.casino.wheel.WheelBlockEntity;
import com.ultra.megamod.feature.casino.wheel.WheelGame;
import com.ultra.megamod.feature.casino.wheel.WheelSegment;
import com.ultra.megamod.feature.economy.EconomyManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record WheelBetPayload(BlockPos wheelPos, int segmentOrdinal, int amount) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<WheelBetPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("megamod", "wheel_bet"));

    public static final StreamCodec<FriendlyByteBuf, WheelBetPayload> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public WheelBetPayload decode(FriendlyByteBuf buf) {
                    BlockPos pos = buf.readBlockPos();
                    int segOrdinal = buf.readInt();
                    int amount = buf.readInt();
                    return new WheelBetPayload(pos, segOrdinal, amount);
                }

                @Override
                public void encode(FriendlyByteBuf buf, WheelBetPayload payload) {
                    buf.writeBlockPos(payload.wheelPos());
                    buf.writeInt(payload.segmentOrdinal());
                    buf.writeInt(payload.amount());
                }
            };

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleOnServer(WheelBetPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) {
                return;
            }
            ServerLevel level = (ServerLevel) serverPlayer.level();
            BlockEntity be = level.getBlockEntity(payload.wheelPos());
            if (!(be instanceof WheelBlockEntity wheelBE)) {
                return;
            }

            WheelGame game = wheelBE.getOrCreateGame();

            // Validate that the wheel is accepting bets
            if (!game.isAcceptingBets()) {
                return;
            }

            // Validate segment ordinal
            WheelSegment[] segments = WheelSegment.values();
            if (payload.segmentOrdinal() < 0 || payload.segmentOrdinal() >= segments.length) {
                return;
            }
            WheelSegment segment = segments[payload.segmentOrdinal()];

            // Validate amount
            if (payload.amount() <= 0) {
                return;
            }

            EconomyManager eco = EconomyManager.get(level);
            game.placeBet(serverPlayer.getUUID(), segment, payload.amount(), eco, level);
        });
    }
}
