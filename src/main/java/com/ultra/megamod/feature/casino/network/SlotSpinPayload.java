package com.ultra.megamod.feature.casino.network;

import com.google.gson.JsonArray;
import com.ultra.megamod.feature.casino.CasinoManager;
import com.ultra.megamod.feature.casino.slots.SlotBetConfig;
import com.ultra.megamod.feature.casino.slots.SlotLines;
import com.ultra.megamod.feature.casino.slots.SlotMachineBlockEntity;
import com.ultra.megamod.feature.casino.slots.SlotSpinEngine;
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

public record SlotSpinPayload(BlockPos machinePos) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SlotSpinPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("megamod", "slot_spin"));

    public static final StreamCodec<FriendlyByteBuf, SlotSpinPayload> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public SlotSpinPayload decode(FriendlyByteBuf buf) {
                    return new SlotSpinPayload(buf.readBlockPos());
                }

                @Override
                public void encode(FriendlyByteBuf buf, SlotSpinPayload payload) {
                    buf.writeBlockPos(payload.machinePos());
                }
            };

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleOnServer(SlotSpinPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) {
                return;
            }
            ServerLevel level = (ServerLevel) serverPlayer.level();
            BlockEntity be = level.getBlockEntity(payload.machinePos());
            if (!(be instanceof SlotMachineBlockEntity slotBE)) {
                return;
            }

            UUID playerId = serverPlayer.getUUID();

            // Validate the player is the occupant
            if (!slotBE.isUsedBy(playerId)) {
                return;
            }

            EconomyManager eco = EconomyManager.get(level);
            CasinoManager casinoMgr = CasinoManager.get(level);

            int betIndex = slotBE.getBetIndex();
            int lineMode = slotBE.getLineMode();
            int totalBet = SlotBetConfig.getTotalBet(betIndex, lineMode);

            // Check if player can afford
            if (!com.ultra.megamod.feature.casino.chips.ChipManager.get(level).spendChips(playerId, totalBet)) {
                return;
            }

            // Record the wager
            casinoMgr.recordWager(playerId, totalBet, "slots");

            // Perform the spin
            boolean forceJackpot = casinoMgr.isAlwaysWinSlots(playerId);
            SlotSpinEngine.SpinResult spinResult = SlotSpinEngine.doSpin(betIndex, lineMode, forceJackpot);

            int totalWin = spinResult.totalWin();

            // Build wins JSON array
            JsonArray winsJson = new JsonArray();
            for (SlotLines.LineResult win : spinResult.wins()) {
                com.google.gson.JsonObject winObj = new com.google.gson.JsonObject();
                winObj.addProperty("line", win.lineIndex());
                winObj.addProperty("symbol", win.symbol().name());
                winObj.addProperty("count", win.matchCount());
                winObj.addProperty("multiplier", win.multiplier());
                winsJson.add(winObj);
            }

            if (totalWin > 0) {
                com.ultra.megamod.feature.casino.chips.ChipManager.get(level).addChipsByValue(playerId, totalWin);
                casinoMgr.recordWin(playerId, totalWin, "slots");
            } else {
                casinoMgr.recordLoss(playerId, totalBet, "slots");
            }

            int newWallet = com.ultra.megamod.feature.casino.chips.ChipManager.get(level).getBalance(playerId);

            // Save stats
            casinoMgr.saveToDisk(level);
            eco.saveToDisk(level);

            // Send result back to client
            SlotResultPayload resultPayload = new SlotResultPayload(
                    spinResult.stops()[0],
                    spinResult.stops()[1],
                    spinResult.stops()[2],
                    totalWin,
                    newWallet,
                    winsJson.toString()
            );
            PacketDistributor.sendToPlayer(serverPlayer, resultPayload);
        });
    }
}
