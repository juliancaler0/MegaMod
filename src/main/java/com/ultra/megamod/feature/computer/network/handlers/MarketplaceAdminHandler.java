package com.ultra.megamod.feature.computer.network.handlers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ultra.megamod.feature.computer.admin.AdminSystem;
import com.ultra.megamod.feature.computer.network.ComputerDataPayload;
import com.ultra.megamod.feature.economy.EconomyManager;
import com.ultra.megamod.feature.marketplace.MarketplaceManager;
import com.ultra.megamod.feature.marketplace.MarketplaceManager.MarketListing;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.*;

/**
 * Network handler for the marketplace admin panel. Provides listing overview, moderation, and fraud detection.
 */
public class MarketplaceAdminHandler {

    public static boolean handle(ServerPlayer player, String action, String jsonData,
                                  ServerLevel level, EconomyManager eco) {
        if (!action.startsWith("marketplace_admin_")) return false;
        if (!AdminSystem.isAdmin(player)) return false;

        switch (action) {
            case "marketplace_admin_request": {
                sendMarketplaceData(player, level, eco);
                return true;
            }
            case "marketplace_admin_remove": {
                handleRemoveListing(player, jsonData, level, eco);
                return true;
            }
            default:
                return false;
        }
    }

    private static void sendMarketplaceData(ServerPlayer player, ServerLevel level, EconomyManager eco) {
        JsonObject root = new JsonObject();

        try {
            MarketplaceManager mgr = MarketplaceManager.get(level);
            mgr.cleanExpired(eco, level);

            List<MarketListing> allListings = mgr.getAllActiveListings();
            root.addProperty("totalListings", allListings.size());

            int totalValue = 0;
            int wtsCount = 0;
            int wtbCount = 0;
            Map<String, Integer> itemCounts = new HashMap<>();

            JsonArray listingsArr = new JsonArray();
            for (MarketListing listing : allListings) {
                JsonObject lObj = new JsonObject();
                lObj.addProperty("id", listing.id);
                lObj.addProperty("type", listing.type.name());

                // Resolve player name
                ServerPlayer seller = level.getServer().getPlayerList().getPlayer(listing.sellerUuid);
                String playerName = seller != null ? seller.getGameProfile().name() : listing.sellerName;

                lObj.addProperty("player", playerName);
                lObj.addProperty("playerUuid", listing.sellerUuid.toString());
                lObj.addProperty("item", listing.itemId);
                lObj.addProperty("quantity", listing.quantity);
                lObj.addProperty("pricePerUnit", listing.pricePerUnit);
                lObj.addProperty("posted", listing.postedTime);
                lObj.addProperty("expiry", 0);
                listingsArr.add(lObj);

                totalValue += listing.pricePerUnit * listing.quantity;
                itemCounts.merge(listing.itemId, 1, Integer::sum);

                if (listing.type == MarketplaceManager.ListingType.WTS) wtsCount++;
                else wtbCount++;
            }

            root.add("listings", listingsArr);
            root.addProperty("totalValue", totalValue);
            root.addProperty("wtsCount", wtsCount);
            root.addProperty("wtbCount", wtbCount);
            root.addProperty("avgPrice", allListings.isEmpty() ? 0 : totalValue / allListings.size());

            // Most traded item
            String mostTraded = "";
            int maxCount = 0;
            for (Map.Entry<String, Integer> entry : itemCounts.entrySet()) {
                if (entry.getValue() > maxCount) {
                    maxCount = entry.getValue();
                    mostTraded = entry.getKey();
                }
            }
            root.addProperty("mostTradedItem", mostTraded);

            // Trade history from activity log
            JsonArray historyArr = new JsonArray();
            List<MarketplaceManager.TradeActivity> history = MarketplaceManager.getTradeHistory();
            int historyLimit = Math.min(history.size(), 50);
            for (int i = 0; i < historyLimit; i++) {
                MarketplaceManager.TradeActivity ta = history.get(i);
                JsonObject h = new JsonObject();
                h.addProperty("action", ta.action());
                h.addProperty("player", ta.playerName());
                h.addProperty("item", ta.itemName());
                h.addProperty("quantity", ta.quantity());
                h.addProperty("pricePerUnit", ta.pricePerUnit());
                h.addProperty("timestamp", ta.timestamp());
                historyArr.add(h);
            }
            root.add("tradeHistory", historyArr);

            // Price watch: most listed items with avg prices
            Map<String, int[]> priceData = new HashMap<>(); // itemId -> [totalPrice, count]
            for (MarketListing listing : allListings) {
                priceData.computeIfAbsent(listing.itemId, k -> new int[]{0, 0});
                int[] data = priceData.get(listing.itemId);
                data[0] += listing.pricePerUnit;
                data[1]++;
            }
            JsonArray priceWatchArr = new JsonArray();
            priceData.entrySet().stream()
                    .sorted((a, b) -> Integer.compare(b.getValue()[1], a.getValue()[1]))
                    .limit(10)
                    .forEach(e -> {
                        JsonObject pw = new JsonObject();
                        pw.addProperty("itemId", e.getKey());
                        pw.addProperty("avgPrice", e.getValue()[1] > 0 ? e.getValue()[0] / e.getValue()[1] : 0);
                        pw.addProperty("listingCount", e.getValue()[1]);
                        priceWatchArr.add(pw);
                    });
            root.add("priceWatch", priceWatchArr);
            root.add("fraudFlags", new JsonArray());

        } catch (Exception e) {
            root.addProperty("totalListings", 0);
            root.addProperty("totalValue", 0);
            root.addProperty("wtsCount", 0);
            root.addProperty("wtbCount", 0);
            root.addProperty("avgPrice", 0);
            root.addProperty("mostTradedItem", "");
            root.add("listings", new JsonArray());
            root.add("priceWatch", new JsonArray());
            root.add("tradeHistory", new JsonArray());
            root.add("fraudFlags", new JsonArray());
        }

        sendResponse(player, "marketplace_admin_data", root.toString(), eco);
    }

    private static void handleRemoveListing(ServerPlayer admin, String listingId, ServerLevel level, EconomyManager eco) {
        try {
            int id = Integer.parseInt(listingId.trim());
            MarketplaceManager mgr = MarketplaceManager.get(level);
            MarketListing listing = mgr.getListingById(id);
            if (listing != null && listing.active) {
                // Admin force-cancel: use the listing owner's UUID
                mgr.cancelListing(listing.sellerUuid, id, eco);
                mgr.saveToDisk(level);
            }
        } catch (Exception ignored) {}

        // Send updated data
        sendMarketplaceData(admin, level, eco);
    }

    private static void sendResponse(ServerPlayer player, String dataType, String json, EconomyManager eco) {
        int wallet = eco.getWallet(player.getUUID());
        int bank = eco.getBank(player.getUUID());
        PacketDistributor.sendToPlayer(player,
                (CustomPacketPayload) new ComputerDataPayload(dataType, json, wallet, bank));
    }
}
