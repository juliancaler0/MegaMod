package com.ultra.megamod.feature.casino.network;

import com.ultra.megamod.feature.casino.CasinoManager;
import com.ultra.megamod.feature.casino.blackjack.BlackjackAction;
import com.ultra.megamod.feature.casino.blackjack.BlackjackTable;
import com.ultra.megamod.feature.casino.blackjack.BlackjackTableBlockEntity;
import com.ultra.megamod.feature.economy.EconomyManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record BlackjackActionPayload(BlockPos tablePos, String action, String data) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<BlackjackActionPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("megamod", "blackjack_action"));

    public static final StreamCodec<FriendlyByteBuf, BlackjackActionPayload> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public BlackjackActionPayload decode(FriendlyByteBuf buf) {
                    BlockPos pos = buf.readBlockPos();
                    String action = buf.readUtf(256);
                    String data = buf.readUtf(32767);
                    return new BlackjackActionPayload(pos, action, data);
                }

                @Override
                public void encode(FriendlyByteBuf buf, BlackjackActionPayload payload) {
                    buf.writeBlockPos(payload.tablePos());
                    buf.writeUtf(payload.action(), 256);
                    buf.writeUtf(payload.data(), 32767);
                }
            };

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleOnServer(BlackjackActionPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) {
                return;
            }
            ServerLevel level = (ServerLevel) serverPlayer.level();
            BlockEntity be = level.getBlockEntity(payload.tablePos());
            if (!(be instanceof BlackjackTableBlockEntity tableBE)) {
                return;
            }

            // Parse the action string to the enum
            // Handle special non-enum actions first
            String actionStr = payload.action().toUpperCase();
            if ("REOPEN".equals(actionStr)) {
                // Reopen the blackjack screen for this player
                BlackjackTable table2 = tableBE.getOrCreateGame();
                if (table2.isPlayerSeated(serverPlayer.getUUID())) {
                    PacketDistributor.sendToPlayer(serverPlayer, new OpenBlackjackPayload(payload.tablePos()));
                    PacketDistributor.sendToPlayer(serverPlayer, new BlackjackSyncPayload(
                            table2.toJsonForPlayer(serverPlayer.getUUID()).toString()));
                }
                return;
            }
            if ("REOPEN_WHEEL".equals(actionStr)) {
                PacketDistributor.sendToPlayer(serverPlayer, new OpenWheelPayload(payload.tablePos()));
                return;
            }

            BlackjackAction bjAction;
            try {
                bjAction = BlackjackAction.valueOf(actionStr);
            } catch (IllegalArgumentException e) {
                return;
            }

            BlackjackTable table = tableBE.getOrCreateGame();
            UUID playerId = serverPlayer.getUUID();
            EconomyManager eco = EconomyManager.get(level);
            CasinoManager casinoMgr = CasinoManager.get(level);

            // Dispatch the action to the table
            switch (bjAction) {
                case JOIN -> table.joinTable(serverPlayer);
                case LEAVE -> table.leaveTable(playerId);
                case BET -> {
                    int betAmount = 0;
                    try {
                        betAmount = Integer.parseInt(payload.data());
                    } catch (NumberFormatException ignored) {
                    }
                    if (betAmount > 0) {
                        table.placeBet(playerId, betAmount, eco, level);
                    }
                }
                default -> table.handleAction(playerId, bjAction, eco, level, casinoMgr);
            }

            // After any action, broadcast per-player sync to all seated players
            BlackjackTable.PlayerSeat[] seats = table.getSeats();
            for (BlackjackTable.PlayerSeat seat : seats) {
                if (seat != null) {
                    ServerPlayer seatedPlayer = level.getServer().getPlayerList().getPlayer(seat.playerId);
                    if (seatedPlayer != null) {
                        String playerJson = table.toJsonForPlayer(seat.playerId).toString();
                        PacketDistributor.sendToPlayer(seatedPlayer, new BlackjackSyncPayload(playerJson));
                    }
                }
            }

            // Also send to the requesting player in case they aren't in the seats array
            if (table.isPlayerSeated(playerId)) {
                String reqJson = table.toJsonForPlayer(playerId).toString();
                PacketDistributor.sendToPlayer(serverPlayer, new BlackjackSyncPayload(reqJson));
            }
        });
    }
}
