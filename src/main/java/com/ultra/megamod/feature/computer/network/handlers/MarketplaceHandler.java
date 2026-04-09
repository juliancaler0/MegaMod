package com.ultra.megamod.feature.computer.network.handlers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ultra.megamod.feature.computer.network.ComputerDataPayload;
import com.ultra.megamod.feature.dimensions.DimensionHelper;
import com.ultra.megamod.feature.dimensions.MegaModDimensions;
import com.ultra.megamod.feature.dimensions.PocketBuilder;
import com.ultra.megamod.feature.economy.EconomyManager;
import com.ultra.megamod.feature.marketplace.MarketplaceManager;
import com.ultra.megamod.feature.marketplace.MarketplaceManager.ListingType;
import com.ultra.megamod.feature.marketplace.MarketplaceManager.MarketListing;
import com.ultra.megamod.feature.marketplace.MarketplaceManager.MarketNotification;
import com.ultra.megamod.feature.marketplace.MarketplaceManager.TradeFloorInvite;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;
import java.util.UUID;

public class MarketplaceHandler {

    private static boolean tradingFloorBuilt = false;
    private static final BlockPos FLOOR_ORIGIN = new BlockPos(5000, 64, 0);
    private static final BlockPos FLOOR_SPAWN = new BlockPos(5017, 65, 15);

    public static boolean handle(ServerPlayer player, String action, String jsonData, ServerLevel level, EconomyManager eco) {
        switch (action) {
            case "market_request": {
                MarketplaceManager mgr = MarketplaceManager.get(level);
                mgr.cleanExpired(eco, level);
                sendMarketData(player, level, eco);
                return true;
            }
            case "market_post": {
                MarketplaceManager.get(level).cleanExpired(eco, level);
                handlePost(player, jsonData, level, eco);
                return true;
            }
            case "market_cancel": {
                handleCancel(player, jsonData, level, eco);
                return true;
            }
            case "market_contact": {
                handleContact(player, jsonData, level, eco);
                return true;
            }
            case "market_my_listings": {
                sendMyListings(player, level, eco);
                return true;
            }
            case "market_search": {
                handleSearch(player, jsonData, level, eco);
                return true;
            }
            case "market_clear_notifs": {
                MarketplaceManager mgr = MarketplaceManager.get(level);
                mgr.markNotificationsRead(player.getUUID());
                mgr.saveToDisk(level);
                sendMarketData(player, level, eco);
                return true;
            }
            case "market_enter_floor": {
                handleEnterFloor(player, level);
                return true;
            }
            case "market_accept_invite": {
                handleAcceptInvite(player, level, eco);
                return true;
            }
            case "market_decline_invite": {
                handleDeclineInvite(player, level, eco);
                return true;
            }
            default:
                return false;
        }
    }

    // --- Action handlers ---

    private static void handlePost(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        // Format: "type:itemId:quantity:pricePerUnit"
        // type is WTS or WTB
        // itemId is namespace:path (contains a colon)
        // So minimum 5 parts: type, namespace, path, quantity, pricePerUnit
        String[] parts = jsonData.split(":");
        if (parts.length < 5) {
            sendResult(player, false, "Invalid listing format.", eco);
            return;
        }

        String typeStr = parts[0].trim().toUpperCase();
        ListingType type;
        try {
            type = ListingType.valueOf(typeStr);
        } catch (IllegalArgumentException e) {
            sendResult(player, false, "Invalid listing type. Use WTS or WTB.", eco);
            return;
        }

        // Reconstruct item ID (namespace:path)
        String itemId = parts[1] + ":" + parts[2];
        int quantity;
        int pricePerUnit;
        try {
            quantity = Integer.parseInt(parts[3].trim());
            pricePerUnit = Integer.parseInt(parts[4].trim());
        } catch (NumberFormatException e) {
            sendResult(player, false, "Invalid quantity or price.", eco);
            return;
        }

        // Validate item exists
        Item item = BuiltInRegistries.ITEM.getValue(Identifier.parse(itemId));
        if (item == Items.AIR) {
            sendResult(player, false, "Unknown item: " + itemId, eco);
            return;
        }

        String itemName = new ItemStack(item).getHoverName().getString();

        MarketplaceManager mgr = MarketplaceManager.get(level);
        String error = mgr.createListing(player.getUUID(), player.getGameProfile().name(),
                type, itemId, itemName, quantity, pricePerUnit, eco);

        if (error != null) {
            sendResult(player, false, error, eco);
            return;
        }

        mgr.saveToDisk(level);
        eco.saveToDisk(level);

        String typeLabel = type == ListingType.WTS ? "sell" : "buy";
        sendResult(player, true, "Listed " + quantity + "x " + itemName + " to " + typeLabel + " at " + pricePerUnit + " MC each!", eco);
    }

    private static void handleCancel(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        int listingId;
        try {
            listingId = Integer.parseInt(jsonData.trim());
        } catch (NumberFormatException e) {
            sendResult(player, false, "Invalid listing ID.", eco);
            return;
        }

        MarketplaceManager mgr = MarketplaceManager.get(level);
        String error = mgr.cancelListing(player.getUUID(), listingId, eco);

        if (error != null) {
            sendResult(player, false, error, eco);
            return;
        }

        mgr.saveToDisk(level);
        eco.saveToDisk(level);

        MarketListing listing = mgr.getListingById(listingId);
        if (listing != null && listing.type == ListingType.WTB) {
            sendResult(player, true, "Listing cancelled. " + listing.getTotalPrice() + " MC refunded to bank.", eco);
        } else {
            sendResult(player, true, "Listing cancelled.", eco);
        }
    }

    private static void handleContact(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        int listingId;
        try {
            listingId = Integer.parseInt(jsonData.trim());
        } catch (NumberFormatException e) {
            sendResult(player, false, "Invalid listing ID.", eco);
            return;
        }

        MarketplaceManager mgr = MarketplaceManager.get(level);
        MarketListing listing = mgr.getListingById(listingId);

        if (listing == null || !listing.active) {
            sendResult(player, false, "Listing not found or no longer active.", eco);
            return;
        }

        // Send trade floor invite instead of just a notification
        String error = mgr.createInvite(player.getUUID(), player.getGameProfile().name(), listingId, level);
        if (error != null) {
            sendResult(player, false, error, eco);
            return;
        }

        // Also store a notification so they see it in inbox
        mgr.expressInterest(player.getUUID(), player.getGameProfile().name(), listingId);
        mgr.saveToDisk(level);

        // Send chat message to listing owner
        ServerPlayer owner = level.getServer().getPlayerList().getPlayer(listing.sellerUuid);
        if (owner != null) {
            String actionVerb = listing.type == ListingType.WTS ? "buy" : "sell to";
            owner.sendSystemMessage(Component.literal(
                    "[Market] " + player.getGameProfile().name() + " wants to " + actionVerb
                            + " your " + listing.quantity + "x " + listing.itemName
                            + "! Open the Marketplace to accept the Trade Floor invite.")
                    .withStyle(ChatFormatting.GOLD));

            sendResult(player, true, "Trade Floor invite sent to " + listing.sellerName + "! Waiting for them to accept...", eco);
        } else {
            sendResult(player, false, listing.sellerName + " is offline. They must be online to receive an invite.", eco);
        }
    }

    private static void handleAcceptInvite(ServerPlayer player, ServerLevel level, EconomyManager eco) {
        MarketplaceManager mgr = MarketplaceManager.get(level);
        TradeFloorInvite invite = mgr.acceptInvite(player.getUUID());

        if (invite == null) {
            sendResult(player, false, "No pending invite, or it has expired.", eco);
            return;
        }

        // Check inviter is still online
        ServerPlayer inviter = level.getServer().getPlayerList().getPlayer(invite.inviterUuid);
        if (inviter == null) {
            sendResult(player, false, invite.inviterName + " is no longer online.", eco);
            return;
        }

        // Build trade floor if needed
        ServerLevel tradingLevel = level.getServer().getLevel(MegaModDimensions.TRADING);
        if (tradingLevel == null) {
            sendResult(player, false, "Trading dimension is not available!", eco);
            return;
        }

        if (!tradingFloorBuilt) {
            PocketBuilder.buildTradingFloor(tradingLevel, FLOOR_ORIGIN);
            tradingFloorBuilt = true;
        }

        // Teleport both players to the trade floor
        inviter.closeContainer();
        player.closeContainer();

        DimensionHelper.teleportToDimension(inviter, MegaModDimensions.TRADING, FLOOR_SPAWN, 0f, 0f);
        DimensionHelper.teleportToDimension(player, MegaModDimensions.TRADING, FLOOR_SPAWN, 0f, 0f);

        inviter.sendSystemMessage(Component.literal(
                "[Market] " + player.getGameProfile().name() + " accepted your invite! Welcome to the Trade Floor!")
                .withStyle(ChatFormatting.GOLD));
        player.sendSystemMessage(Component.literal(
                "[Market] You accepted " + invite.inviterName + "'s invite! Welcome to the Trade Floor!")
                .withStyle(ChatFormatting.GOLD));
    }

    private static void handleDeclineInvite(ServerPlayer player, ServerLevel level, EconomyManager eco) {
        MarketplaceManager mgr = MarketplaceManager.get(level);
        TradeFloorInvite invite = mgr.getPendingInvite(player.getUUID());

        if (invite == null) {
            sendResult(player, false, "No pending invite to decline.", eco);
            return;
        }

        mgr.declineInvite(player.getUUID());

        // Notify inviter if online
        ServerPlayer inviter = level.getServer().getPlayerList().getPlayer(invite.inviterUuid);
        if (inviter != null) {
            inviter.sendSystemMessage(Component.literal(
                    "[Market] " + player.getGameProfile().name() + " declined your Trade Floor invite.")
                    .withStyle(ChatFormatting.RED));
        }

        sendResult(player, true, "Invite declined.", eco);
    }

    private static void handleSearch(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        String query = jsonData.trim();
        if (query.isEmpty()) {
            sendMarketData(player, level, eco);
            return;
        }

        MarketplaceManager mgr = MarketplaceManager.get(level);
        List<MarketListing> results = mgr.searchListings(query);

        JsonObject root = new JsonObject();

        JsonArray listingsArr = new JsonArray();
        for (MarketListing l : results) {
            listingsArr.add(listingToJson(l, player, level));
        }
        root.add("listings", listingsArr);

        // Include player's own listings
        JsonArray myArr = new JsonArray();
        for (MarketListing l : mgr.getPlayerListings(player.getUUID())) {
            myArr.add(listingToJson(l, player, level));
        }
        root.add("myListings", myArr);

        // Include notifications
        addNotificationsToJson(root, mgr, player.getUUID());

        sendResponse(player, "market_data", root.toString(), eco);
    }

    private static void handleEnterFloor(ServerPlayer player, ServerLevel level) {
        ServerLevel tradingLevel = level.getServer().getLevel(MegaModDimensions.TRADING);
        if (tradingLevel == null) {
            player.sendSystemMessage(Component.literal("Trading dimension is not available!").withStyle(ChatFormatting.RED));
            return;
        }

        if (!tradingFloorBuilt) {
            player.sendSystemMessage(Component.literal("Building the Trade Floor...").withStyle(ChatFormatting.GOLD));
            PocketBuilder.buildTradingFloor(tradingLevel, FLOOR_ORIGIN);
            tradingFloorBuilt = true;
        }

        player.closeContainer();
        DimensionHelper.teleportToDimension(player, MegaModDimensions.TRADING, FLOOR_SPAWN, 0f, 0f);
        player.sendSystemMessage(Component.literal("Welcome to the Trade Floor!").withStyle(ChatFormatting.GOLD));
    }

    public static void resetFloorState() {
        tradingFloorBuilt = false;
    }

    // --- Data responses ---

    private static void sendMarketData(ServerPlayer player, ServerLevel level, EconomyManager eco) {
        MarketplaceManager mgr = MarketplaceManager.get(level);
        UUID playerUuid = player.getUUID();

        JsonObject root = new JsonObject();

        // All active listings
        JsonArray listingsArr = new JsonArray();
        for (MarketListing l : mgr.getAllActiveListings()) {
            listingsArr.add(listingToJson(l, player, level));
        }
        root.add("listings", listingsArr);

        // Player's own listings
        JsonArray myArr = new JsonArray();
        for (MarketListing l : mgr.getPlayerListings(playerUuid)) {
            myArr.add(listingToJson(l, player, level));
        }
        root.add("myListings", myArr);

        // Notifications
        addNotificationsToJson(root, mgr, playerUuid);

        // Pending invite for this player
        TradeFloorInvite invite = mgr.getPendingInvite(playerUuid);
        if (invite != null) {
            JsonObject inviteObj = new JsonObject();
            inviteObj.addProperty("inviterName", invite.inviterName);
            inviteObj.addProperty("inviterUuid", invite.inviterUuid.toString());
            inviteObj.addProperty("listingId", invite.listingId);
            long remainingSec = Math.max(0, (60000L - (System.currentTimeMillis() - invite.timestamp)) / 1000L);
            inviteObj.addProperty("remainingSec", remainingSec);
            MarketListing invListing = mgr.getListingById(invite.listingId);
            if (invListing != null) {
                inviteObj.addProperty("itemName", invListing.itemName);
                inviteObj.addProperty("quantity", invListing.quantity);
                inviteObj.addProperty("listingType", invListing.type.name());
            }
            root.add("pendingInvite", inviteObj);
        }

        sendResponse(player, "market_data", root.toString(), eco);
    }

    private static void sendMyListings(ServerPlayer player, ServerLevel level, EconomyManager eco) {
        MarketplaceManager mgr = MarketplaceManager.get(level);

        JsonObject root = new JsonObject();
        JsonArray myArr = new JsonArray();
        for (MarketListing l : mgr.getPlayerListings(player.getUUID())) {
            myArr.add(listingToJson(l, player, level));
        }
        root.add("myListings", myArr);

        sendResponse(player, "market_my_listings", root.toString(), eco);
    }

    // --- JSON helpers ---

    private static JsonObject listingToJson(MarketListing l, ServerPlayer viewer, ServerLevel level) {
        JsonObject obj = new JsonObject();
        obj.addProperty("id", l.id);
        obj.addProperty("type", l.type.name());
        obj.addProperty("sellerName", l.sellerName);
        obj.addProperty("sellerUuid", l.sellerUuid.toString());
        obj.addProperty("itemId", l.itemId);
        obj.addProperty("itemName", l.itemName);
        obj.addProperty("quantity", l.quantity);
        obj.addProperty("pricePerUnit", l.pricePerUnit);
        obj.addProperty("totalPrice", l.getTotalPrice());
        obj.addProperty("postedTime", l.postedTime);
        obj.addProperty("timeLeft", MarketplaceManager.formatTimeRemaining(l.postedTime));
        obj.addProperty("isOwn", l.sellerUuid.equals(viewer.getUUID()));

        // Online status
        ServerPlayer sellerOnline = level.getServer().getPlayerList().getPlayer(l.sellerUuid);
        obj.addProperty("online", sellerOnline != null);

        return obj;
    }

    private static void addNotificationsToJson(JsonObject root, MarketplaceManager mgr, UUID playerUuid) {
        List<MarketNotification> notifs = mgr.getNotifications(playerUuid);
        JsonArray notifsArr = new JsonArray();
        for (MarketNotification n : notifs) {
            JsonObject no = new JsonObject();
            no.addProperty("fromName", n.fromName);
            no.addProperty("fromUuid", n.fromUuid.toString());
            no.addProperty("listingId", n.listingId);
            no.addProperty("message", n.message);
            no.addProperty("timestamp", n.timestamp);
            no.addProperty("read", n.read);

            // Format time
            long elapsed = System.currentTimeMillis() - n.timestamp;
            long seconds = elapsed / 1000;
            String timeAgo;
            if (seconds < 60) timeAgo = seconds + "s ago";
            else if (seconds < 3600) timeAgo = (seconds / 60) + "m ago";
            else if (seconds < 86400) timeAgo = (seconds / 3600) + "h ago";
            else timeAgo = (seconds / 86400) + "d ago";
            no.addProperty("timeAgo", timeAgo);

            notifsArr.add(no);
        }
        root.add("notifications", notifsArr);
        root.addProperty("unreadCount", mgr.getUnreadNotificationCount(playerUuid));
    }

    // --- Response helpers ---

    private static void sendResult(ServerPlayer player, boolean success, String message, EconomyManager eco) {
        JsonObject obj = new JsonObject();
        obj.addProperty("success", success);
        obj.addProperty("message", message);
        sendResponse(player, "market_result", obj.toString(), eco);
    }

    private static void sendResponse(ServerPlayer player, String type, String json, EconomyManager eco) {
        int wallet = eco.getWallet(player.getUUID());
        int bank = eco.getBank(player.getUUID());
        PacketDistributor.sendToPlayer(player, new ComputerDataPayload(type, json, wallet, bank));
    }
}
