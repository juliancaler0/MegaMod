package com.ultra.megamod.feature.computer.network.handlers;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ultra.megamod.feature.computer.network.ComputerDataPayload;
import com.ultra.megamod.feature.economy.EconomyManager;
import com.ultra.megamod.feature.prestige.MasteryMarkManager;
import com.ultra.megamod.feature.prestige.PrestigeRewardManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.UUID;

/**
 * Server-side handler for the Prestige Shop computer app.
 * Handles browsing, purchasing, and activating prestige rewards.
 */
public class PrestigeShopHandler {

    public static boolean handle(ServerPlayer player, String action, String jsonData, ServerLevel level, EconomyManager eco) {
        switch (action) {
            case "prestige_shop_request" -> {
                sendShopData(player, level, eco);
                return true;
            }
            case "prestige_shop_purchase" -> {
                handlePurchase(player, jsonData, level, eco);
                return true;
            }
            case "prestige_shop_activate" -> {
                handleActivate(player, jsonData, level, eco);
                return true;
            }
        }
        return false;
    }

    private static void sendShopData(ServerPlayer player, ServerLevel level, EconomyManager eco) {
        UUID uuid = player.getUUID();
        ServerLevel overworld = player.level().getServer().overworld();
        PrestigeRewardManager prm = PrestigeRewardManager.get(overworld);
        MasteryMarkManager marks = MasteryMarkManager.get(overworld);

        // Build response JSON using the manager's toJson for reward data
        String rewardJson = prm.toJson(uuid);

        // Wrap it with marks balance
        JsonObject root = JsonParser.parseString(rewardJson).getAsJsonObject();
        root.addProperty("marks", marks.getMarks(uuid));

        sendResponse(player, "prestige_shop_data", root.toString(), eco);
    }

    private static void handlePurchase(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        String rewardId = jsonData.trim();
        ServerLevel overworld = player.level().getServer().overworld();
        PrestigeRewardManager prm = PrestigeRewardManager.get(overworld);

        String error = prm.purchaseReward(player, rewardId);
        if (error != null) {
            sendResult(player, false, error, eco);
        } else {
            prm.saveToDisk(overworld);
            sendResult(player, true, "Purchased successfully!", eco);
        }
    }

    private static void handleActivate(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        try {
            JsonObject obj = JsonParser.parseString(jsonData).getAsJsonObject();
            String category = obj.get("category").getAsString();
            String rewardId = obj.get("rewardId").getAsString();

            UUID uuid = player.getUUID();
            ServerLevel overworld = player.level().getServer().overworld();
            PrestigeRewardManager prm = PrestigeRewardManager.get(overworld);

            // Allow deactivation by passing empty rewardId
            if (rewardId.isEmpty()) {
                prm.clearActive(uuid, category);
                prm.saveToDisk(overworld);
                sendResult(player, true, "Deactivated.", eco);
                return;
            }

            // Verify ownership
            if (!prm.hasPurchased(uuid, rewardId)) {
                sendResult(player, false, "You haven't purchased this reward.", eco);
                return;
            }

            prm.setActive(uuid, category, rewardId);
            prm.saveToDisk(overworld);
            sendResult(player, true, "Activated!", eco);
        } catch (Exception e) {
            sendResult(player, false, "Error activating reward.", eco);
        }
    }

    private static void sendResult(ServerPlayer player, boolean success, String message, EconomyManager eco) {
        JsonObject obj = new JsonObject();
        obj.addProperty("success", success);
        obj.addProperty("message", message);
        sendResponse(player, "prestige_shop_result", obj.toString(), eco);
    }

    private static void sendResponse(ServerPlayer player, String type, String json, EconomyManager eco) {
        int wallet = eco.getWallet(player.getUUID());
        int bank = eco.getBank(player.getUUID());
        PacketDistributor.sendToPlayer(player, new ComputerDataPayload(type, json, wallet, bank));
    }
}
