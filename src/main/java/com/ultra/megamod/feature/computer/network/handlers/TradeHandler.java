package com.ultra.megamod.feature.computer.network.handlers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ultra.megamod.feature.computer.network.ComputerDataPayload;
import com.ultra.megamod.feature.economy.EconomyManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TradeHandler {

    private static final Map<UUID, TradeOffer> activeOffers = new ConcurrentHashMap<>(); // offerer UUID -> offer
    private static final long OFFER_TIMEOUT_MS = 120_000; // 120 seconds

    // Cached online player info to avoid rebuilding on every trade request
    private static List<PlayerInfo> cachedPlayers = null;
    private static long cacheTime = 0;
    private static final long CACHE_TTL_TICKS = 20; // refresh every second (20 ticks)

    private record PlayerInfo(String name, UUID uuid) {}

    private static List<PlayerInfo> getOnlinePlayers(net.minecraft.server.MinecraftServer server) {
        long now = server.getTickCount();
        if (cachedPlayers == null || now - cacheTime > CACHE_TTL_TICKS) {
            List<PlayerInfo> list = new ArrayList<>();
            for (ServerPlayer p : server.getPlayerList().getPlayers()) {
                list.add(new PlayerInfo(p.getGameProfile().name(), p.getUUID()));
            }
            cachedPlayers = list;
            cacheTime = now;
        }
        return cachedPlayers;
    }

    /** Invalidate cache on player join/leave for immediate accuracy. */
    public static void invalidatePlayerCache() {
        cachedPlayers = null;
    }

    private record TradeOffer(UUID offerer, UUID target, int coinsOffered, String itemOfferedId,
                              int itemCount, long timestamp) {}

    public static boolean handle(ServerPlayer player, String action, String jsonData, ServerLevel level, EconomyManager eco) {
        switch (action) {
            case "trade_request": {
                cleanExpiredOffers();
                sendTradeData(player, level, eco);
                return true;
            }
            case "trade_send_offer": {
                cleanExpiredOffers();
                handleSendOffer(player, jsonData, level, eco);
                return true;
            }
            case "trade_accept": {
                cleanExpiredOffers();
                handleAccept(player, jsonData, level, eco);
                return true;
            }
            case "trade_decline": {
                cleanExpiredOffers();
                handleDecline(player, jsonData, level, eco);
                return true;
            }
            case "trade_cancel": {
                cleanExpiredOffers();
                handleCancel(player, level, eco);
                return true;
            }
            default:
                return false;
        }
    }

    // --- Action handlers ---

    private static void handleSendOffer(ServerPlayer player, String data, ServerLevel level, EconomyManager eco) {
        // Format: targetUuid:coinsOffered:itemId:itemCount
        String[] parts = data.split(":", 4);
        if (parts.length < 4) {
            sendResult(player, false, "Invalid offer format.", eco);
            return;
        }

        UUID targetUuid;
        try {
            targetUuid = UUID.fromString(parts[0].trim());
        } catch (IllegalArgumentException e) {
            sendResult(player, false, "Invalid target player.", eco);
            return;
        }

        int coinsOffered;
        try {
            coinsOffered = Integer.parseInt(parts[1].trim());
        } catch (NumberFormatException e) {
            sendResult(player, false, "Invalid coin amount.", eco);
            return;
        }

        String itemId = parts[2].trim();
        int itemCount;
        try {
            itemCount = Integer.parseInt(parts[3].trim());
        } catch (NumberFormatException e) {
            sendResult(player, false, "Invalid item count.", eco);
            return;
        }

        UUID playerUuid = player.getUUID();

        // Cannot trade with self
        if (playerUuid.equals(targetUuid)) {
            sendResult(player, false, "Cannot trade with yourself.", eco);
            return;
        }

        // Target must be online
        ServerPlayer target = level.getServer().getPlayerList().getPlayer(targetUuid);
        if (target == null) {
            sendResult(player, false, "That player is offline.", eco);
            return;
        }

        // Cannot have an active outgoing offer already
        if (activeOffers.containsKey(playerUuid)) {
            sendResult(player, false, "You already have an active offer. Cancel it first.", eco);
            return;
        }

        // Validate coins
        if (coinsOffered < 0) {
            sendResult(player, false, "Coin amount cannot be negative.", eco);
            return;
        }
        if (coinsOffered > 0) {
            int wallet = eco.getWallet(playerUuid);
            if (wallet < coinsOffered) {
                sendResult(player, false, "Not enough coins. You have " + wallet + " MC.", eco);
                return;
            }
        }

        // Validate item
        if (!itemId.isEmpty() && itemCount > 0) {
            Item item = BuiltInRegistries.ITEM.getValue(Identifier.parse(itemId));
            if (item == null || item == Items.AIR) {
                sendResult(player, false, "Invalid item.", eco);
                return;
            }
            int haveCount = countItemInInventory(player, item);
            if (haveCount < itemCount) {
                String itemName = new ItemStack(item).getHoverName().getString();
                sendResult(player, false, "You only have " + haveCount + " " + itemName + ".", eco);
                return;
            }
        }

        // Must offer something
        if (coinsOffered <= 0 && (itemId.isEmpty() || itemCount <= 0)) {
            sendResult(player, false, "You must offer coins or an item.", eco);
            return;
        }

        // Create the offer
        activeOffers.put(playerUuid, new TradeOffer(playerUuid, targetUuid, coinsOffered, itemId, itemCount, System.currentTimeMillis()));

        String targetName = target.getGameProfile().name();
        sendResult(player, true, "Trade offer sent to " + targetName + "!", eco);

        // Notify target
        target.sendSystemMessage(
            Component.literal(player.getGameProfile().name() + " sent you a trade offer! Use the Computer to respond.")
                .withStyle(ChatFormatting.AQUA)
        );
    }

    private static void handleAccept(ServerPlayer player, String offererUuidStr, ServerLevel level, EconomyManager eco) {
        UUID offererUuid;
        try {
            offererUuid = UUID.fromString(offererUuidStr.trim());
        } catch (IllegalArgumentException e) {
            sendResult(player, false, "Invalid player.", eco);
            return;
        }

        UUID playerUuid = player.getUUID();

        // Find the offer targeting this player
        TradeOffer offer = activeOffers.get(offererUuid);
        if (offer == null || !offer.target.equals(playerUuid)) {
            sendResult(player, false, "No trade offer from that player.", eco);
            return;
        }

        // Check expiry
        long elapsed = System.currentTimeMillis() - offer.timestamp;
        if (elapsed > OFFER_TIMEOUT_MS) {
            activeOffers.remove(offererUuid);
            sendResult(player, false, "That trade offer has expired.", eco);
            return;
        }

        // Offerer must still be online
        ServerPlayer offerer = level.getServer().getPlayerList().getPlayer(offererUuid);
        if (offerer == null) {
            activeOffers.remove(offererUuid);
            sendResult(player, false, "The offering player is no longer online.", eco);
            return;
        }

        // Re-verify offerer has the coins
        if (offer.coinsOffered > 0) {
            int offererWallet = eco.getWallet(offererUuid);
            if (offererWallet < offer.coinsOffered) {
                activeOffers.remove(offererUuid);
                sendResult(player, false, "The offerer no longer has enough coins.", eco);
                offerer.sendSystemMessage(
                    Component.literal("Your trade offer was rejected: insufficient coins.")
                        .withStyle(ChatFormatting.RED)
                );
                return;
            }
        }

        // Re-verify offerer has the item
        if (!offer.itemOfferedId.isEmpty() && offer.itemCount > 0) {
            Item item = BuiltInRegistries.ITEM.getValue(Identifier.parse(offer.itemOfferedId));
            if (item == null || item == Items.AIR) {
                activeOffers.remove(offererUuid);
                sendResult(player, false, "Invalid item in offer.", eco);
                return;
            }
            int haveCount = countItemInInventory(offerer, item);
            if (haveCount < offer.itemCount) {
                activeOffers.remove(offererUuid);
                sendResult(player, false, "The offerer no longer has enough of the item.", eco);
                offerer.sendSystemMessage(
                    Component.literal("Your trade offer was rejected: insufficient items.")
                        .withStyle(ChatFormatting.RED)
                );
                return;
            }
        }

        // --- Execute the trade (synchronized to prevent race conditions) ---
        synchronized (eco) {
            // Transfer coins: offerer -> accepter
            if (offer.coinsOffered > 0) {
                int offererWallet = eco.getWallet(offererUuid);
                if (offererWallet < offer.coinsOffered) {
                    activeOffers.remove(offererUuid);
                    sendResult(player, false, "The offerer no longer has enough coins.", eco);
                    return;
                }
                int accepterWallet = eco.getWallet(playerUuid);
                eco.setWallet(offererUuid, offererWallet - offer.coinsOffered);
                eco.setWallet(playerUuid, accepterWallet + offer.coinsOffered);
            }

            // Transfer item: offerer -> accepter
            if (!offer.itemOfferedId.isEmpty() && offer.itemCount > 0) {
                Item item = BuiltInRegistries.ITEM.getValue(Identifier.parse(offer.itemOfferedId));
                if (item != null && item != Items.AIR) {
                    removeItemFromInventory(offerer, item, offer.itemCount);
                    ItemStack giveStack = new ItemStack(item, offer.itemCount);
                    if (!player.getInventory().add(giveStack)) {
                        // Inventory full, drop on ground
                        player.spawnAtLocation((ServerLevel) player.level(), giveStack);
                    }
                }
            }
        }

        // Remove the offer
        activeOffers.remove(offererUuid);

        // Build result message
        String offererName = offerer.getGameProfile().name();
        String accepterName = player.getGameProfile().name();
        StringBuilder tradeDesc = new StringBuilder();
        if (offer.coinsOffered > 0) {
            tradeDesc.append(offer.coinsOffered).append(" MC");
        }
        if (!offer.itemOfferedId.isEmpty() && offer.itemCount > 0) {
            Item item = BuiltInRegistries.ITEM.getValue(Identifier.parse(offer.itemOfferedId));
            if (item != null && item != Items.AIR) {
                String itemName = new ItemStack(item).getHoverName().getString();
                if (tradeDesc.length() > 0) tradeDesc.append(" + ");
                tradeDesc.append(offer.itemCount).append("x ").append(itemName);
            }
        }

        sendResult(player, true, "Trade accepted! Received " + tradeDesc + " from " + offererName + ".", eco);
        offerer.sendSystemMessage(
            Component.literal(accepterName + " accepted your trade! Sent " + tradeDesc + ".")
                .withStyle(ChatFormatting.GREEN)
        );
    }

    private static void handleDecline(ServerPlayer player, String offererUuidStr, ServerLevel level, EconomyManager eco) {
        UUID offererUuid;
        try {
            offererUuid = UUID.fromString(offererUuidStr.trim());
        } catch (IllegalArgumentException e) {
            sendResult(player, false, "Invalid player.", eco);
            return;
        }

        UUID playerUuid = player.getUUID();

        TradeOffer offer = activeOffers.get(offererUuid);
        if (offer == null || !offer.target.equals(playerUuid)) {
            sendResult(player, false, "No trade offer from that player.", eco);
            return;
        }

        activeOffers.remove(offererUuid);
        sendResult(player, true, "Trade offer declined.", eco);

        // Notify offerer if online
        ServerPlayer offerer = level.getServer().getPlayerList().getPlayer(offererUuid);
        if (offerer != null) {
            offerer.sendSystemMessage(
                Component.literal(player.getGameProfile().name() + " declined your trade offer.")
                    .withStyle(ChatFormatting.RED)
            );
        }
    }

    private static void handleCancel(ServerPlayer player, ServerLevel level, EconomyManager eco) {
        UUID playerUuid = player.getUUID();
        TradeOffer offer = activeOffers.remove(playerUuid);
        if (offer == null) {
            sendResult(player, false, "You don't have an active trade offer.", eco);
            return;
        }

        sendResult(player, true, "Trade offer cancelled.", eco);

        // Notify target if online
        ServerPlayer target = level.getServer().getPlayerList().getPlayer(offer.target);
        if (target != null) {
            target.sendSystemMessage(
                Component.literal(player.getGameProfile().name() + " cancelled their trade offer.")
                    .withStyle(ChatFormatting.YELLOW)
            );
        }
    }

    // --- Data response ---

    private static void sendTradeData(ServerPlayer player, ServerLevel level, EconomyManager eco) {
        UUID playerUuid = player.getUUID();
        JsonObject root = new JsonObject();

        // Online players list (exclude self) - uses cached player info
        JsonArray playersArr = new JsonArray();
        for (PlayerInfo info : getOnlinePlayers(level.getServer())) {
            if (info.uuid.equals(playerUuid)) continue;
            JsonObject p = new JsonObject();
            p.addProperty("name", info.name);
            p.addProperty("uuid", info.uuid.toString());
            playersArr.add(p);
        }
        root.add("onlinePlayers", playersArr);

        // Incoming offers (offers where target == this player)
        JsonArray incomingArr = new JsonArray();
        for (Map.Entry<UUID, TradeOffer> entry : activeOffers.entrySet()) {
            TradeOffer offer = entry.getValue();
            if (!offer.target.equals(playerUuid)) continue;

            long elapsed = System.currentTimeMillis() - offer.timestamp;
            if (elapsed > OFFER_TIMEOUT_MS) continue;

            int timeLeft = (int) ((OFFER_TIMEOUT_MS - elapsed) / 1000);

            JsonObject o = new JsonObject();
            String fromName = getPlayerName(offer.offerer, level);
            o.addProperty("from", fromName);
            o.addProperty("fromUuid", offer.offerer.toString());
            o.addProperty("coins", offer.coinsOffered);
            o.addProperty("itemId", offer.itemOfferedId);

            // Resolve item display name
            String itemName = "";
            if (!offer.itemOfferedId.isEmpty()) {
                Item item = BuiltInRegistries.ITEM.getValue(Identifier.parse(offer.itemOfferedId));
                if (item != null && item != Items.AIR) {
                    itemName = new ItemStack(item).getHoverName().getString();
                }
            }
            o.addProperty("itemName", itemName);
            o.addProperty("itemCount", offer.itemCount);
            o.addProperty("timeLeft", timeLeft);
            incomingArr.add(o);
        }
        root.add("incomingOffers", incomingArr);

        // Outgoing offer (this player's active offer)
        TradeOffer outgoing = activeOffers.get(playerUuid);
        if (outgoing != null) {
            long elapsed = System.currentTimeMillis() - outgoing.timestamp;
            if (elapsed <= OFFER_TIMEOUT_MS) {
                JsonObject out = new JsonObject();
                String toName = getPlayerName(outgoing.target, level);
                out.addProperty("to", toName);
                out.addProperty("coins", outgoing.coinsOffered);
                out.addProperty("itemId", outgoing.itemOfferedId);

                String itemName = "";
                if (!outgoing.itemOfferedId.isEmpty()) {
                    Item item = BuiltInRegistries.ITEM.getValue(Identifier.parse(outgoing.itemOfferedId));
                    if (item != null && item != Items.AIR) {
                        itemName = new ItemStack(item).getHoverName().getString();
                    }
                }
                out.addProperty("itemName", itemName);
                out.addProperty("itemCount", outgoing.itemCount);
                int timeLeft = (int) ((OFFER_TIMEOUT_MS - elapsed) / 1000);
                out.addProperty("timeLeft", timeLeft);
                root.add("outgoingOffer", out);
            }
        }

        sendResponse(player, "trade_data", root.toString(), eco);
    }

    private static void sendResult(ServerPlayer player, boolean success, String message, EconomyManager eco) {
        JsonObject obj = new JsonObject();
        obj.addProperty("success", success);
        obj.addProperty("message", message);
        sendResponse(player, "trade_result", obj.toString(), eco);
    }

    private static void sendResponse(ServerPlayer player, String type, String json, EconomyManager eco) {
        int wallet = eco.getWallet(player.getUUID());
        int bank = eco.getBank(player.getUUID());
        PacketDistributor.sendToPlayer(player, new ComputerDataPayload(type, json, wallet, bank));
    }

    // --- Helpers ---

    private static String getPlayerName(UUID uuid, ServerLevel level) {
        ServerPlayer online = level.getServer().getPlayerList().getPlayer(uuid);
        if (online != null) {
            return online.getGameProfile().name();
        }
        return "Unknown";
    }

    private static int countItemInInventory(ServerPlayer player, Item item) {
        int count = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.getItem() == item) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private static void removeItemFromInventory(ServerPlayer player, Item item, int amount) {
        int remaining = amount;
        for (int i = 0; i < player.getInventory().getContainerSize() && remaining > 0; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.getItem() == item) {
                int take = Math.min(remaining, stack.getCount());
                stack.shrink(take);
                remaining -= take;
                if (stack.isEmpty()) {
                    player.getInventory().setItem(i, ItemStack.EMPTY);
                }
            }
        }
    }

    private static void cleanExpiredOffers() {
        long now = System.currentTimeMillis();
        activeOffers.entrySet().removeIf(e -> (now - e.getValue().timestamp) > OFFER_TIMEOUT_MS);
    }

    /**
     * Called when a player disconnects to cancel any offers involving them.
     */
    public static void onPlayerDisconnect(ServerPlayer player) {
        UUID uuid = player.getUUID();
        // Remove outgoing offer
        activeOffers.remove(uuid);
        // Remove any offers targeting this player
        activeOffers.entrySet().removeIf(e -> e.getValue().target.equals(uuid));
        // Invalidate cached player list
        invalidatePlayerCache();
    }
}
