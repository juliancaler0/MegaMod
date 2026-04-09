package com.ultra.megamod.feature.marketplace.block;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ultra.megamod.feature.computer.network.ComputerDataPayload;
import com.ultra.megamod.feature.economy.EconomyManager;
import com.ultra.megamod.feature.marketplace.MarketplaceRegistry;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TradingTerminalBlockEntity extends BlockEntity {

    private static final int TIMEOUT_TICKS = 120 * 20; // 120 seconds at 20 tps
    private static final double MAX_DISTANCE = 8.0; // blocks

    // Trader slots
    private UUID trader1Uuid;
    private UUID trader2Uuid;

    // Offers
    private final List<ItemOffer> trader1Items = new ArrayList<>();
    private int trader1Coins = 0;
    private boolean trader1Confirmed = false;

    private final List<ItemOffer> trader2Items = new ArrayList<>();
    private int trader2Coins = 0;
    private boolean trader2Confirmed = false;

    // Timing
    private int ticksSinceLastActivity = 0;
    private boolean tradeInProgress = false;

    public record ItemOffer(String itemId, String itemName, int count) {}

    public TradingTerminalBlockEntity(BlockPos pos, BlockState state) {
        super(MarketplaceRegistry.TRADING_TERMINAL_BE.get(), pos, state);
    }

    // --- Player interaction ---

    public void onPlayerInteract(ServerPlayer player, ServerLevel level) {
        UUID playerUuid = player.getUUID();

        // Already a trader?
        if (playerUuid.equals(trader1Uuid) || playerUuid.equals(trader2Uuid)) {
            // Send current state
            sendTerminalState(level);
            return;
        }

        // Join as trader1
        if (trader1Uuid == null) {
            trader1Uuid = playerUuid;
            tradeInProgress = true;
            ticksSinceLastActivity = 0;
            player.sendSystemMessage(Component.literal("You joined the trading terminal. Waiting for a trade partner...")
                    .withStyle(ChatFormatting.GREEN));
            sendTerminalState(level);
            return;
        }

        // Join as trader2
        if (trader2Uuid == null) {
            trader2Uuid = playerUuid;
            ticksSinceLastActivity = 0;
            player.sendSystemMessage(Component.literal("You joined the trading terminal!")
                    .withStyle(ChatFormatting.GREEN));

            // Notify trader1
            ServerPlayer t1 = level.getServer().getPlayerList().getPlayer(trader1Uuid);
            if (t1 != null) {
                t1.sendSystemMessage(Component.literal(player.getGameProfile().name() + " joined the trading terminal!")
                        .withStyle(ChatFormatting.GREEN));
            }

            sendTerminalState(level);
            return;
        }

        // Full
        player.sendSystemMessage(Component.literal("This trading terminal is already in use.")
                .withStyle(ChatFormatting.RED));
    }

    // --- Handle actions from computer action handler (prefixed with terminal_) ---

    public boolean handleAction(ServerPlayer player, String action, String jsonData, ServerLevel level, EconomyManager eco) {
        UUID playerUuid = player.getUUID();
        if (!playerUuid.equals(trader1Uuid) && !playerUuid.equals(trader2Uuid)) {
            return false; // not part of this terminal
        }

        ticksSinceLastActivity = 0;
        boolean isTrader1 = playerUuid.equals(trader1Uuid);

        switch (action) {
            case "terminal_offer_items": {
                handleOfferItems(player, jsonData, isTrader1, level);
                return true;
            }
            case "terminal_offer_coins": {
                handleOfferCoins(player, jsonData, isTrader1, level, eco);
                return true;
            }
            case "terminal_confirm": {
                handleConfirm(player, isTrader1, level, eco);
                return true;
            }
            case "terminal_cancel": {
                handleCancelTrade(player, level, eco);
                return true;
            }
            case "terminal_state": {
                sendTerminalState(level);
                return true;
            }
            default:
                return false;
        }
    }

    private void handleOfferItems(ServerPlayer player, String jsonData, boolean isTrader1, ServerLevel level) {
        // jsonData format: "itemId1:count1,itemId2:count2,..."
        List<ItemOffer> offers = isTrader1 ? trader1Items : trader2Items;
        offers.clear();

        // Unconfirm both players when offer changes
        trader1Confirmed = false;
        trader2Confirmed = false;

        if (jsonData != null && !jsonData.isEmpty()) {
            String[] entries = jsonData.split(",");
            for (String entry : entries) {
                // Each entry: "namespace:path:count" — 3+ parts
                String[] parts = entry.split(":");
                if (parts.length >= 3) {
                    String itemId = parts[0] + ":" + parts[1];
                    int count;
                    try {
                        count = Integer.parseInt(parts[2].trim());
                    } catch (NumberFormatException e) {
                        continue;
                    }

                    if (count <= 0) continue;

                    // Validate item
                    Item item = BuiltInRegistries.ITEM.getValue(Identifier.parse(itemId));
                    if (item == Items.AIR) continue;

                    // Verify player has enough
                    int available = countItemInInventory(player, item);
                    int actualCount = Math.min(count, available);
                    if (actualCount <= 0) continue;

                    String itemName = new ItemStack(item).getHoverName().getString();
                    offers.add(new ItemOffer(itemId, itemName, actualCount));
                }
            }
        }

        sendTerminalState(level);
    }

    private void handleOfferCoins(ServerPlayer player, String jsonData, boolean isTrader1, ServerLevel level, EconomyManager eco) {
        int coins;
        try {
            coins = Integer.parseInt(jsonData.trim());
        } catch (NumberFormatException e) {
            return;
        }

        if (coins < 0) coins = 0;

        // Verify player has enough in wallet
        int wallet = eco.getWallet(player.getUUID());
        coins = Math.min(coins, wallet);

        if (isTrader1) {
            trader1Coins = coins;
        } else {
            trader2Coins = coins;
        }

        // Unconfirm both
        trader1Confirmed = false;
        trader2Confirmed = false;

        sendTerminalState(level);
    }

    private void handleConfirm(ServerPlayer player, boolean isTrader1, ServerLevel level, EconomyManager eco) {
        if (trader1Uuid == null || trader2Uuid == null) {
            player.sendSystemMessage(Component.literal("Waiting for a trade partner.")
                    .withStyle(ChatFormatting.YELLOW));
            return;
        }

        if (isTrader1) {
            trader1Confirmed = true;
        } else {
            trader2Confirmed = true;
        }

        // Check if both confirmed
        if (trader1Confirmed && trader2Confirmed) {
            executeTrade(level, eco);
        } else {
            sendTerminalState(level);

            // Notify the other player
            UUID otherUuid = isTrader1 ? trader2Uuid : trader1Uuid;
            ServerPlayer other = level.getServer().getPlayerList().getPlayer(otherUuid);
            if (other != null) {
                other.sendSystemMessage(Component.literal(player.getGameProfile().name() + " confirmed the trade!")
                        .withStyle(ChatFormatting.AQUA));
            }
        }
    }

    private void executeTrade(ServerLevel level, EconomyManager eco) {
        ServerPlayer p1 = level.getServer().getPlayerList().getPlayer(trader1Uuid);
        ServerPlayer p2 = level.getServer().getPlayerList().getPlayer(trader2Uuid);

        if (p1 == null || p2 == null) {
            // Someone went offline, cancel
            if (p1 != null) {
                p1.sendSystemMessage(Component.literal("Trade cancelled: other player went offline.")
                        .withStyle(ChatFormatting.RED));
            }
            if (p2 != null) {
                p2.sendSystemMessage(Component.literal("Trade cancelled: other player went offline.")
                        .withStyle(ChatFormatting.RED));
            }
            resetTerminal();
            return;
        }

        // Verify both players still have their offered items and coins
        // Check trader1 items
        for (ItemOffer offer : trader1Items) {
            Item item = BuiltInRegistries.ITEM.getValue(Identifier.parse(offer.itemId));
            if (item == Items.AIR || countItemInInventory(p1, item) < offer.count) {
                p1.sendSystemMessage(Component.literal("Trade failed: you no longer have enough " + offer.itemName + ".")
                        .withStyle(ChatFormatting.RED));
                p2.sendSystemMessage(Component.literal("Trade failed: other player doesn't have enough items.")
                        .withStyle(ChatFormatting.RED));
                resetTerminal();
                return;
            }
        }

        // Check trader2 items
        for (ItemOffer offer : trader2Items) {
            Item item = BuiltInRegistries.ITEM.getValue(Identifier.parse(offer.itemId));
            if (item == Items.AIR || countItemInInventory(p2, item) < offer.count) {
                p2.sendSystemMessage(Component.literal("Trade failed: you no longer have enough " + offer.itemName + ".")
                        .withStyle(ChatFormatting.RED));
                p1.sendSystemMessage(Component.literal("Trade failed: other player doesn't have enough items.")
                        .withStyle(ChatFormatting.RED));
                resetTerminal();
                return;
            }
        }

        // Check coins
        if (trader1Coins > 0 && eco.getWallet(trader1Uuid) < trader1Coins) {
            p1.sendSystemMessage(Component.literal("Trade failed: insufficient coins.")
                    .withStyle(ChatFormatting.RED));
            p2.sendSystemMessage(Component.literal("Trade failed: other player doesn't have enough coins.")
                    .withStyle(ChatFormatting.RED));
            resetTerminal();
            return;
        }
        if (trader2Coins > 0 && eco.getWallet(trader2Uuid) < trader2Coins) {
            p2.sendSystemMessage(Component.literal("Trade failed: insufficient coins.")
                    .withStyle(ChatFormatting.RED));
            p1.sendSystemMessage(Component.literal("Trade failed: other player doesn't have enough coins.")
                    .withStyle(ChatFormatting.RED));
            resetTerminal();
            return;
        }

        // --- Execute atomic swap ---

        // Transfer items: trader1 -> trader2
        for (ItemOffer offer : trader1Items) {
            Item item = BuiltInRegistries.ITEM.getValue(Identifier.parse(offer.itemId));
            if (item != Items.AIR) {
                removeItemsFromInventory(p1, item, offer.count);
                giveItemsToPlayer(p2, item, offer.count, level);
            }
        }

        // Transfer items: trader2 -> trader1
        for (ItemOffer offer : trader2Items) {
            Item item = BuiltInRegistries.ITEM.getValue(Identifier.parse(offer.itemId));
            if (item != Items.AIR) {
                removeItemsFromInventory(p2, item, offer.count);
                giveItemsToPlayer(p1, item, offer.count, level);
            }
        }

        // Transfer coins
        if (trader1Coins > 0) {
            int w1 = eco.getWallet(trader1Uuid);
            int w2 = eco.getWallet(trader2Uuid);
            eco.setWallet(trader1Uuid, w1 - trader1Coins);
            eco.setWallet(trader2Uuid, w2 + trader1Coins);
        }
        if (trader2Coins > 0) {
            int w1 = eco.getWallet(trader1Uuid);
            int w2 = eco.getWallet(trader2Uuid);
            eco.setWallet(trader2Uuid, w2 - trader2Coins);
            eco.setWallet(trader1Uuid, w1 + trader2Coins);
        }

        eco.saveToDisk(level);

        // Build trade summary
        String t1Name = p1.getGameProfile().name();
        String t2Name = p2.getGameProfile().name();

        p1.sendSystemMessage(Component.literal("Trade complete with " + t2Name + "!")
                .withStyle(ChatFormatting.GREEN));
        p2.sendSystemMessage(Component.literal("Trade complete with " + t1Name + "!")
                .withStyle(ChatFormatting.GREEN));
        com.ultra.megamod.feature.quests.QuestEventListener.onMarketplaceTrade(p1.getUUID(), (net.minecraft.server.level.ServerLevel) p1.level());
        com.ultra.megamod.feature.quests.QuestEventListener.onMarketplaceTrade(p2.getUUID(), (net.minecraft.server.level.ServerLevel) p2.level());

        // Send trade_complete response to both
        JsonObject completeJson = new JsonObject();
        completeJson.addProperty("success", true);
        completeJson.addProperty("message", "Trade completed successfully!");

        PacketDistributor.sendToPlayer(p1, new ComputerDataPayload("terminal_complete",
                completeJson.toString(), eco.getWallet(trader1Uuid), eco.getBank(trader1Uuid)));
        PacketDistributor.sendToPlayer(p2, new ComputerDataPayload("terminal_complete",
                completeJson.toString(), eco.getWallet(trader2Uuid), eco.getBank(trader2Uuid)));

        resetTerminal();
    }

    private void handleCancelTrade(ServerPlayer player, ServerLevel level, EconomyManager eco) {
        UUID otherUuid = player.getUUID().equals(trader1Uuid) ? trader2Uuid : trader1Uuid;

        // Notify other player
        if (otherUuid != null) {
            ServerPlayer other = level.getServer().getPlayerList().getPlayer(otherUuid);
            if (other != null) {
                other.sendSystemMessage(Component.literal(player.getGameProfile().name() + " cancelled the trade.")
                        .withStyle(ChatFormatting.YELLOW));

                JsonObject cancelJson = new JsonObject();
                cancelJson.addProperty("cancelled", true);
                cancelJson.addProperty("message", player.getGameProfile().name() + " cancelled the trade.");
                PacketDistributor.sendToPlayer(other, new ComputerDataPayload("terminal_cancelled",
                        cancelJson.toString(), eco.getWallet(otherUuid), eco.getBank(otherUuid)));
            }
        }

        player.sendSystemMessage(Component.literal("Trade cancelled.")
                .withStyle(ChatFormatting.YELLOW));

        JsonObject cancelJson = new JsonObject();
        cancelJson.addProperty("cancelled", true);
        cancelJson.addProperty("message", "You cancelled the trade.");
        PacketDistributor.sendToPlayer(player, new ComputerDataPayload("terminal_cancelled",
                cancelJson.toString(), eco.getWallet(player.getUUID()), eco.getBank(player.getUUID())));

        resetTerminal();
    }

    // --- Server tick ---

    public void serverTick(ServerLevel level) {
        if (!tradeInProgress) return;

        ticksSinceLastActivity++;

        // Timeout check
        if (ticksSinceLastActivity >= TIMEOUT_TICKS) {
            // Notify and reset
            notifyTrader(trader1Uuid, "Trade timed out.", level);
            notifyTrader(trader2Uuid, "Trade timed out.", level);
            resetTerminal();
            return;
        }

        // Distance check every 20 ticks (1 second)
        if (ticksSinceLastActivity % 20 == 0) {
            BlockPos pos = this.getBlockPos();

            if (trader1Uuid != null) {
                ServerPlayer t1 = level.getServer().getPlayerList().getPlayer(trader1Uuid);
                if (t1 == null || t1.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) > MAX_DISTANCE * MAX_DISTANCE) {
                    notifyTrader(trader2Uuid, "Other player walked away. Trade cancelled.", level);
                    if (t1 != null) {
                        t1.sendSystemMessage(Component.literal("You walked away from the terminal. Trade cancelled.")
                                .withStyle(ChatFormatting.YELLOW));
                    }
                    resetTerminal();
                    return;
                }
            }

            if (trader2Uuid != null) {
                ServerPlayer t2 = level.getServer().getPlayerList().getPlayer(trader2Uuid);
                if (t2 == null || t2.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) > MAX_DISTANCE * MAX_DISTANCE) {
                    notifyTrader(trader1Uuid, "Other player walked away. Trade cancelled.", level);
                    if (t2 != null) {
                        t2.sendSystemMessage(Component.literal("You walked away from the terminal. Trade cancelled.")
                                .withStyle(ChatFormatting.YELLOW));
                    }
                    resetTerminal();
                    return;
                }
            }
        }
    }

    private void notifyTrader(UUID uuid, String message, ServerLevel level) {
        if (uuid == null) return;
        ServerPlayer player = level.getServer().getPlayerList().getPlayer(uuid);
        if (player != null) {
            player.sendSystemMessage(Component.literal(message).withStyle(ChatFormatting.YELLOW));
        }
    }

    // --- State sync ---

    private void sendTerminalState(ServerLevel level) {
        JsonObject state = buildStateJson(level);

        if (trader1Uuid != null) {
            ServerPlayer t1 = level.getServer().getPlayerList().getPlayer(trader1Uuid);
            if (t1 != null) {
                EconomyManager eco = EconomyManager.get(level);
                PacketDistributor.sendToPlayer(t1, new ComputerDataPayload("terminal_state",
                        state.toString(), eco.getWallet(trader1Uuid), eco.getBank(trader1Uuid)));
            }
        }

        if (trader2Uuid != null) {
            ServerPlayer t2 = level.getServer().getPlayerList().getPlayer(trader2Uuid);
            if (t2 != null) {
                EconomyManager eco = EconomyManager.get(level);
                PacketDistributor.sendToPlayer(t2, new ComputerDataPayload("terminal_state",
                        state.toString(), eco.getWallet(trader2Uuid), eco.getBank(trader2Uuid)));
            }
        }
    }

    private JsonObject buildStateJson(ServerLevel level) {
        JsonObject root = new JsonObject();

        root.addProperty("trader1Uuid", trader1Uuid != null ? trader1Uuid.toString() : "");
        root.addProperty("trader2Uuid", trader2Uuid != null ? trader2Uuid.toString() : "");

        // Resolve names
        String t1Name = resolveName(trader1Uuid, level);
        String t2Name = resolveName(trader2Uuid, level);
        root.addProperty("trader1Name", t1Name);
        root.addProperty("trader2Name", t2Name);

        // Trader 1 offer
        JsonObject t1Offer = new JsonObject();
        JsonArray t1ItemsArr = new JsonArray();
        for (ItemOffer offer : trader1Items) {
            JsonObject io = new JsonObject();
            io.addProperty("itemId", offer.itemId);
            io.addProperty("itemName", offer.itemName);
            io.addProperty("count", offer.count);
            t1ItemsArr.add(io);
        }
        t1Offer.add("items", t1ItemsArr);
        t1Offer.addProperty("coins", trader1Coins);
        t1Offer.addProperty("confirmed", trader1Confirmed);
        root.add("trader1Offer", t1Offer);

        // Trader 2 offer
        JsonObject t2Offer = new JsonObject();
        JsonArray t2ItemsArr = new JsonArray();
        for (ItemOffer offer : trader2Items) {
            JsonObject io = new JsonObject();
            io.addProperty("itemId", offer.itemId);
            io.addProperty("itemName", offer.itemName);
            io.addProperty("count", offer.count);
            t2ItemsArr.add(io);
        }
        t2Offer.add("items", t2ItemsArr);
        t2Offer.addProperty("coins", trader2Coins);
        t2Offer.addProperty("confirmed", trader2Confirmed);
        root.add("trader2Offer", t2Offer);

        // Timer
        int secondsRemaining = Math.max(0, (TIMEOUT_TICKS - ticksSinceLastActivity) / 20);
        root.addProperty("timeRemaining", secondsRemaining);
        root.addProperty("blockPosX", this.getBlockPos().getX());
        root.addProperty("blockPosY", this.getBlockPos().getY());
        root.addProperty("blockPosZ", this.getBlockPos().getZ());

        return root;
    }

    private String resolveName(UUID uuid, ServerLevel level) {
        if (uuid == null) return "";
        ServerPlayer player = level.getServer().getPlayerList().getPlayer(uuid);
        if (player != null) return player.getGameProfile().name();
        return "Unknown";
    }

    // --- Reset ---

    private void resetTerminal() {
        trader1Uuid = null;
        trader2Uuid = null;
        trader1Items.clear();
        trader2Items.clear();
        trader1Coins = 0;
        trader2Coins = 0;
        trader1Confirmed = false;
        trader2Confirmed = false;
        ticksSinceLastActivity = 0;
        tradeInProgress = false;
    }

    // --- Inventory helpers ---

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

    private static void removeItemsFromInventory(ServerPlayer player, Item item, int amount) {
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

    private static void giveItemsToPlayer(ServerPlayer player, Item item, int amount, ServerLevel level) {
        int remaining = amount;
        int maxStack = item.getDefaultMaxStackSize();
        while (remaining > 0) {
            int give = Math.min(remaining, maxStack);
            ItemStack stack = new ItemStack(item, give);
            if (!player.getInventory().add(stack)) {
                player.spawnAtLocation(level, stack);
            }
            remaining -= give;
        }
    }

    // --- Public getters for terminal action routing ---

    public UUID getTrader1Uuid() { return trader1Uuid; }
    public UUID getTrader2Uuid() { return trader2Uuid; }
    public boolean isPartOfTrade(UUID playerUuid) {
        return playerUuid.equals(trader1Uuid) || playerUuid.equals(trader2Uuid);
    }
}
