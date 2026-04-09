package com.ultra.megamod.feature.casino.network;

import com.google.gson.JsonObject;
import com.ultra.megamod.feature.casino.CasinoDimensionManager;
import com.ultra.megamod.feature.casino.CasinoManager;
import com.ultra.megamod.feature.computer.network.ComputerDataPayload;
import com.ultra.megamod.feature.economy.EconomyManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Handles computer app actions for the Casino system.
 * Dispatched from the main ComputerActionPayload handler.
 */
public class CasinoHandler {

    public static boolean handle(ServerPlayer player, String action, String jsonData, ServerLevel level, EconomyManager eco) {
        switch (action) {
            case "casino_request" -> {
                CasinoManager mgr = CasinoManager.get(level);
                int[] stats = mgr.getStats(player.getUUID());

                JsonObject json = new JsonObject();
                json.addProperty("totalWagered", stats[0]);
                json.addProperty("totalWon", stats[1]);
                json.addProperty("totalLost", stats[2]);
                json.addProperty("gamesPlayed", stats[3]);
                json.addProperty("biggestWin", stats[4]);
                json.addProperty("slotsPlayed", stats[5]);
                json.addProperty("slotsWon", stats[6]);
                json.addProperty("blackjackPlayed", stats[7]);
                json.addProperty("blackjackWon", stats[8]);
                json.addProperty("wheelPlayed", stats[9]);
                json.addProperty("wheelWon", stats[10]);
                json.addProperty("roulettePlayed", stats[11]);
                json.addProperty("rouletteWon", stats[12]);
                json.addProperty("baccaratPlayed", stats[13]);
                json.addProperty("baccaratWon", stats[14]);
                json.addProperty("crapsPlayed", stats[15]);
                json.addProperty("crapsWon", stats[16]);

                int profitLoss = stats[1] - stats[2];
                json.addProperty("profitLoss", profitLoss);

                PacketDistributor.sendToPlayer(player, new ComputerDataPayload(
                        "casino_stats",
                        json.toString(),
                        eco.getWallet(player.getUUID()),
                        eco.getBank(player.getUUID())
                ));
                return true;
            }
            case "casino_teleport" -> {
                CasinoDimensionManager.enterCasino(player);
                return true;
            }
            default -> {
                return false;
            }
        }
    }
}
