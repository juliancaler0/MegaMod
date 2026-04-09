package com.ultra.megamod.feature.computer.network.handlers;

import com.ultra.megamod.MegaMod;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ultra.megamod.feature.computer.network.ComputerDataPayload;
import com.ultra.megamod.feature.economy.EconomyManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.neoforge.network.PacketDistributor;

import com.ultra.megamod.feature.bountyhunt.BountyHuntManager;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BountyBoardHandler {

    private static final List<Bounty> bounties = Collections.synchronizedList(new ArrayList<>());
    private static int nextBountyId = 1;

    private static boolean loaded = false;
    private static boolean dirty = false;
    private static final String FILE_NAME = "megamod_bounties.dat";
    private static final long EXPIRY_MS = 24 * 60 * 60 * 1000L; // 24 hours
    private static final int MAX_BOUNTIES_PER_PLAYER = 5;

    // Minimum price per item for bounty postings
    private static final Map<String, Integer> MIN_PRICES = new LinkedHashMap<>();
    static {
        // Common drops (1-3 MC each)
        MIN_PRICES.put("minecraft:cobblestone", 1);
        MIN_PRICES.put("minecraft:dirt", 1);
        MIN_PRICES.put("minecraft:sand", 1);
        MIN_PRICES.put("minecraft:gravel", 1);
        MIN_PRICES.put("minecraft:oak_log", 2);
        MIN_PRICES.put("minecraft:spruce_log", 2);
        MIN_PRICES.put("minecraft:birch_log", 2);
        MIN_PRICES.put("minecraft:jungle_log", 2);
        MIN_PRICES.put("minecraft:acacia_log", 2);
        MIN_PRICES.put("minecraft:dark_oak_log", 2);
        MIN_PRICES.put("minecraft:cherry_log", 2);
        MIN_PRICES.put("minecraft:mangrove_log", 2);
        MIN_PRICES.put("minecraft:bamboo", 1);
        MIN_PRICES.put("minecraft:string", 2);
        MIN_PRICES.put("minecraft:feather", 2);
        MIN_PRICES.put("minecraft:leather", 3);
        MIN_PRICES.put("minecraft:wheat", 2);
        MIN_PRICES.put("minecraft:carrot", 2);
        MIN_PRICES.put("minecraft:potato", 2);
        MIN_PRICES.put("minecraft:beetroot", 2);
        MIN_PRICES.put("minecraft:melon_slice", 1);
        MIN_PRICES.put("minecraft:pumpkin", 3);
        MIN_PRICES.put("minecraft:sugar_cane", 2);
        MIN_PRICES.put("minecraft:cactus", 2);
        MIN_PRICES.put("minecraft:bone", 2);
        MIN_PRICES.put("minecraft:gunpowder", 3);
        MIN_PRICES.put("minecraft:rotten_flesh", 1);
        MIN_PRICES.put("minecraft:spider_eye", 2);
        MIN_PRICES.put("minecraft:ink_sac", 2);
        MIN_PRICES.put("minecraft:egg", 2);
        MIN_PRICES.put("minecraft:flint", 2);
        MIN_PRICES.put("minecraft:clay_ball", 2);
        MIN_PRICES.put("minecraft:snowball", 1);

        // Uncommon resources (3-10 MC each)
        MIN_PRICES.put("minecraft:iron_ingot", 5);
        MIN_PRICES.put("minecraft:copper_ingot", 4);
        MIN_PRICES.put("minecraft:gold_ingot", 8);
        MIN_PRICES.put("minecraft:coal", 3);
        MIN_PRICES.put("minecraft:lapis_lazuli", 5);
        MIN_PRICES.put("minecraft:redstone", 4);
        MIN_PRICES.put("minecraft:quartz", 5);
        MIN_PRICES.put("minecraft:amethyst_shard", 6);
        MIN_PRICES.put("minecraft:slime_ball", 5);
        MIN_PRICES.put("minecraft:honey_bottle", 6);
        MIN_PRICES.put("minecraft:honeycomb", 5);
        MIN_PRICES.put("minecraft:prismarine_shard", 6);
        MIN_PRICES.put("minecraft:prismarine_crystals", 7);
        MIN_PRICES.put("minecraft:phantom_membrane", 8);
        MIN_PRICES.put("minecraft:blaze_rod", 10);
        MIN_PRICES.put("minecraft:magma_cream", 8);
        MIN_PRICES.put("minecraft:ghast_tear", 10);
        MIN_PRICES.put("minecraft:ender_pearl", 10);
        MIN_PRICES.put("minecraft:rabbit_foot", 8);
        MIN_PRICES.put("minecraft:nautilus_shell", 10);

        // Rare resources (10-50 MC each)
        MIN_PRICES.put("minecraft:diamond", 25);
        MIN_PRICES.put("minecraft:emerald", 15);
        MIN_PRICES.put("minecraft:ancient_debris", 50);
        MIN_PRICES.put("minecraft:netherite_scrap", 40);
        MIN_PRICES.put("minecraft:netherite_ingot", 150);
        MIN_PRICES.put("minecraft:echo_shard", 30);
        MIN_PRICES.put("minecraft:disc_fragment_5", 20);
        MIN_PRICES.put("minecraft:nether_star", 200);
        MIN_PRICES.put("minecraft:elytra", 300);
        MIN_PRICES.put("minecraft:trident", 150);
        MIN_PRICES.put("minecraft:heart_of_the_sea", 100);
        MIN_PRICES.put("minecraft:totem_of_undying", 100);
        MIN_PRICES.put("minecraft:shulker_shell", 30);
        MIN_PRICES.put("minecraft:wither_skeleton_skull", 40);
        MIN_PRICES.put("minecraft:dragon_egg", 500);
        MIN_PRICES.put("minecraft:enchanted_golden_apple", 200);
        MIN_PRICES.put("minecraft:golden_apple", 20);
        MIN_PRICES.put("minecraft:dragon_breath", 25);
        MIN_PRICES.put("minecraft:experience_bottle", 15);
        // Dungeon-exclusive materials
        MIN_PRICES.put("megamod:void_shard", 30);
        MIN_PRICES.put("megamod:cerulean_ingot", 50);
        MIN_PRICES.put("megamod:crystalline_shard", 60);
        MIN_PRICES.put("megamod:spectral_silk", 80);
        MIN_PRICES.put("megamod:umbra_ingot", 100);
        MIN_PRICES.put("megamod:infernal_essence", 200);
    }
    private static final int DEFAULT_MIN_PRICE = 3;

    // Inner bounty class
    public static class Bounty {
        public int id;
        public UUID posterUuid;
        public String posterName;
        public String itemId;
        public String itemName;
        public int quantity;
        public int priceOffered;
        public long postedTime;
        public boolean fulfilled;
        public UUID fulfillerUuid;
        public String fulfillerName;

        Bounty(int id, UUID posterUuid, String posterName, String itemId, String itemName,
               int quantity, int priceOffered, long postedTime) {
            this.id = id;
            this.posterUuid = posterUuid;
            this.posterName = posterName;
            this.itemId = itemId;
            this.itemName = itemName;
            this.quantity = quantity;
            this.priceOffered = priceOffered;
            this.postedTime = postedTime;
            this.fulfilled = false;
            this.fulfillerUuid = null;
            this.fulfillerName = "";
        }
    }

    public static boolean handle(ServerPlayer player, String action, String jsonData, ServerLevel level, EconomyManager eco) {
        switch (action) {
            case "bounty_request": {
                ensureLoaded(level);
                cleanExpiredBounties(level, eco);
                sendBountyData(player, level, eco);
                return true;
            }
            case "bounty_post": {
                ensureLoaded(level);
                handlePost(player, jsonData, level, eco);
                return true;
            }
            case "bounty_cancel": {
                ensureLoaded(level);
                handleCancel(player, jsonData, level, eco);
                return true;
            }
            case "bounty_fulfill": {
                ensureLoaded(level);
                handleFulfill(player, jsonData, level, eco);
                return true;
            }
            case "bounty_collect": {
                ensureLoaded(level);
                handleCollect(player, jsonData, level, eco);
                return true;
            }
            case "bounty_hunt_request": {
                BountyHuntManager.ensureLoaded(level);
                sendHuntData(player, level, eco);
                return true;
            }
            case "bounty_hunt_accept": {
                BountyHuntManager.ensureLoaded(level);
                handleHuntAccept(player, jsonData, level, eco);
                return true;
            }
            case "bounty_hunt_abandon": {
                BountyHuntManager.ensureLoaded(level);
                handleHuntAbandon(player, jsonData, level, eco);
                return true;
            }
            default:
                return false;
        }
    }

    // --- Action handlers ---

    private static void handlePost(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        // Format: "itemId:quantity:priceOffered"
        String[] parts = jsonData.split(":");
        if (parts.length < 4) {
            // itemId is namespace:path, so at least 4 parts: namespace, path, quantity, price
            sendResult(player, false, "Invalid bounty format.", eco);
            return;
        }

        // Reconstruct item ID (namespace:path) — first two parts
        String itemId = parts[0] + ":" + parts[1];
        int quantity;
        int priceOffered;
        try {
            quantity = Integer.parseInt(parts[2]);
            priceOffered = Integer.parseInt(parts[3]);
        } catch (NumberFormatException e) {
            sendResult(player, false, "Invalid quantity or price.", eco);
            return;
        }

        if (quantity <= 0) {
            sendResult(player, false, "Quantity must be at least 1.", eco);
            return;
        }

        if (priceOffered <= 0) {
            sendResult(player, false, "Price must be at least 1 MC.", eco);
            return;
        }

        // Validate item exists
        Item item = BuiltInRegistries.ITEM.getValue(Identifier.parse(itemId));
        if (item == Items.AIR) {
            sendResult(player, false, "Unknown item: " + itemId, eco);
            return;
        }

        // Get item display name
        String itemName = new ItemStack(item).getHoverName().getString();

        // Check minimum price
        int minPerItem = MIN_PRICES.getOrDefault(itemId, DEFAULT_MIN_PRICE);
        int minTotal = com.ultra.megamod.feature.marketplace.MarketplaceManager.safeMultiply(minPerItem, quantity);
        if (priceOffered < minTotal) {
            sendResult(player, false, "Minimum price is " + minTotal + " MC (" + minPerItem + " MC x " + quantity + ").", eco);
            return;
        }

        // Check max bounties per player
        UUID playerUuid = player.getUUID();
        long activeCount = bounties.stream()
                .filter(b -> b.posterUuid.equals(playerUuid) && !b.fulfilled)
                .count();
        if (activeCount >= MAX_BOUNTIES_PER_PLAYER) {
            sendResult(player, false, "You already have " + MAX_BOUNTIES_PER_PLAYER + " active bounties.", eco);
            return;
        }

        // Check poster has enough coins in bank (escrow)
        int bank = eco.getBank(playerUuid);
        if (bank < priceOffered) {
            sendResult(player, false, "Not enough in bank. Need " + priceOffered + " MC, have " + bank + " MC.", eco);
            return;
        }

        // Deduct from bank (escrow)
        eco.setBank(playerUuid, bank - priceOffered);

        // Create bounty
        Bounty bounty = new Bounty(nextBountyId++, playerUuid, player.getGameProfile().name(),
                itemId, itemName, quantity, priceOffered, System.currentTimeMillis());
        bounties.add(bounty);
        dirty = true;
        saveToDisk(level);

        sendResult(player, true, "Bounty posted: " + quantity + "x " + itemName + " for " + priceOffered + " MC!", eco);
    }

    private static void handleCancel(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        int bountyId;
        try {
            bountyId = Integer.parseInt(jsonData.trim());
        } catch (NumberFormatException e) {
            sendResult(player, false, "Invalid bounty ID.", eco);
            return;
        }

        UUID playerUuid = player.getUUID();
        Bounty bounty = findBountyById(bountyId);

        if (bounty == null) {
            sendResult(player, false, "Bounty not found.", eco);
            return;
        }

        if (!bounty.posterUuid.equals(playerUuid)) {
            sendResult(player, false, "You can only cancel your own bounties.", eco);
            return;
        }

        if (bounty.fulfilled) {
            sendResult(player, false, "Cannot cancel a fulfilled bounty. Collect it instead.", eco);
            return;
        }

        // Refund coins to bank
        int currentBank = eco.getBank(playerUuid);
        eco.setBank(playerUuid, currentBank + bounty.priceOffered);

        bounties.remove(bounty);
        dirty = true;
        saveToDisk(level);

        sendResult(player, true, "Bounty cancelled. " + bounty.priceOffered + " MC refunded to bank.", eco);
    }

    /** Admin force-cancel any bounty, refunding to original poster. */
    public static void cancelBountyAdmin(ServerPlayer admin, int bountyId, ServerLevel level, EconomyManager eco) {
        Bounty bounty = findBountyById(bountyId);
        if (bounty == null) {
            sendResult(admin, false, "Bounty not found.", eco);
            return;
        }
        // Refund to original poster's bank
        int currentBank = eco.getBank(bounty.posterUuid);
        eco.setBank(bounty.posterUuid, currentBank + bounty.priceOffered);
        bounties.remove(bounty);
        dirty = true;
        saveToDisk(level);
        sendResult(admin, true, "Bounty #" + bountyId + " admin-cancelled. " + bounty.priceOffered + " MC refunded to poster.", eco);
    }

    private static void handleFulfill(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        int bountyId;
        try {
            bountyId = Integer.parseInt(jsonData.trim());
        } catch (NumberFormatException e) {
            sendResult(player, false, "Invalid bounty ID.", eco);
            return;
        }

        UUID fulfillerUuid = player.getUUID();
        Bounty bounty = findBountyById(bountyId);

        if (bounty == null) {
            sendResult(player, false, "Bounty not found.", eco);
            return;
        }

        if (bounty.fulfilled) {
            sendResult(player, false, "Bounty already fulfilled.", eco);
            return;
        }

        // Cannot fulfill own bounty
        if (bounty.posterUuid.equals(fulfillerUuid)) {
            sendResult(player, false, "Cannot fulfill your own bounty.", eco);
            return;
        }

        // Check fulfiller has items in inventory
        Item requiredItem = BuiltInRegistries.ITEM.getValue(Identifier.parse(bounty.itemId));
        if (requiredItem == Items.AIR) {
            sendResult(player, false, "Item no longer exists.", eco);
            return;
        }

        int available = countItemInInventory(player, requiredItem);
        if (available < bounty.quantity) {
            sendResult(player, false, "You need " + bounty.quantity + "x " + bounty.itemName + " (have " + available + ").", eco);
            return;
        }

        // Remove items from fulfiller inventory first, then verify removal succeeded
        removeItemsFromInventory(player, requiredItem, bounty.quantity);
        int remaining = countItemInInventory(player, requiredItem);
        if (remaining > available - bounty.quantity) {
            // Removal did not fully succeed — items may have changed between check and remove
            sendResult(player, false, "Failed to remove items. Please try again.", eco);
            return;
        }

        // Give coins to fulfiller wallet only after items have been confirmed removed
        eco.addWallet(fulfillerUuid, bounty.priceOffered);

        // Mark bounty fulfilled
        bounty.fulfilled = true;
        bounty.fulfillerUuid = fulfillerUuid;
        bounty.fulfillerName = player.getGameProfile().name();

        dirty = true;
        saveToDisk(level);

        sendResult(player, true, "Bounty fulfilled! +" + bounty.priceOffered + " MC added to wallet.", eco);

        // Notify poster if online
        ServerPlayer poster = level.getServer().getPlayerList().getPlayer(bounty.posterUuid);
        if (poster != null) {
            poster.sendSystemMessage(Component.literal(player.getGameProfile().name() + " fulfilled your bounty for " +
                    bounty.quantity + "x " + bounty.itemName + "! Collect it from the Bounty Board.").withStyle(ChatFormatting.GREEN));
        }
    }

    private static void handleCollect(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        int bountyId;
        try {
            bountyId = Integer.parseInt(jsonData.trim());
        } catch (NumberFormatException e) {
            sendResult(player, false, "Invalid bounty ID.", eco);
            return;
        }

        UUID playerUuid = player.getUUID();
        Bounty bounty = findBountyById(bountyId);

        if (bounty == null) {
            sendResult(player, false, "Bounty not found.", eco);
            return;
        }

        if (!bounty.posterUuid.equals(playerUuid)) {
            sendResult(player, false, "You can only collect your own bounties.", eco);
            return;
        }

        if (!bounty.fulfilled) {
            sendResult(player, false, "Bounty has not been fulfilled yet.", eco);
            return;
        }

        // Give items to poster
        Item item = BuiltInRegistries.ITEM.getValue(Identifier.parse(bounty.itemId));
        if (item == Items.AIR) {
            sendResult(player, false, "Item no longer exists.", eco);
            return;
        }

        int remaining = bounty.quantity;
        int maxStack = item.getDefaultMaxStackSize();
        while (remaining > 0) {
            int give = Math.min(remaining, maxStack);
            ItemStack stack = new ItemStack(item, give);
            if (!player.getInventory().add(stack)) {
                // Drop on ground if inventory full
                player.spawnAtLocation((ServerLevel) player.level(), stack);
            }
            remaining -= give;
        }

        // Remove bounty from list
        bounties.remove(bounty);
        dirty = true;
        saveToDisk(level);

        sendResult(player, true, "Collected " + bounty.quantity + "x " + bounty.itemName + "!", eco);
    }

    // --- Hunt handlers ---

    private static void sendHuntData(ServerPlayer player, ServerLevel level, EconomyManager eco) {
        UUID playerUuid = player.getUUID();
        long now = System.currentTimeMillis();

        JsonObject root = new JsonObject();

        // Available bounties from today's rotation
        JsonArray availableArr = new JsonArray();
        for (BountyHuntManager.BountyDefinition bd : BountyHuntManager.getAvailableBounties()) {
            JsonObject obj = new JsonObject();
            obj.addProperty("id", bd.id());
            obj.addProperty("mobType", bd.mobType());
            obj.addProperty("mobDisplayName", bd.mobDisplayName());
            obj.addProperty("biomeHint", bd.biomeHint());
            obj.addProperty("reward", bd.reward());
            availableArr.add(obj);
        }
        root.add("availableBounties", availableArr);

        // Player's active hunt bounties
        JsonArray activeArr = new JsonArray();
        for (BountyHuntManager.ActiveBounty ab : BountyHuntManager.getActiveBounties(playerUuid)) {
            JsonObject obj = new JsonObject();
            obj.addProperty("bountyId", ab.bountyId);
            obj.addProperty("targetName", ab.targetName);
            obj.addProperty("completed", ab.completed);
            long elapsed = now - ab.acceptedTime;
            long remainingMs = Math.max(0, 24 * 60 * 60 * 1000L - elapsed);
            long remainingHours = remainingMs / (60 * 60 * 1000L);
            long remainingMinutes = (remainingMs % (60 * 60 * 1000L)) / (60 * 1000L);
            obj.addProperty("timeLeft", remainingHours + "h " + remainingMinutes + "m");
            // Include mob display name for active bounties
            BountyHuntManager.BountyDefinition def = BountyHuntManager.getDefinition(ab.bountyId);
            obj.addProperty("mobDisplayName", def != null ? def.mobDisplayName() : "Unknown");
            activeArr.add(obj);
        }
        root.add("activeBounties", activeArr);

        sendResponse(player, "bounty_hunt_data", root.toString(), eco);
    }

    private static void handleHuntAccept(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        int bountyId;
        try {
            bountyId = Integer.parseInt(jsonData.trim());
        } catch (NumberFormatException e) {
            sendResult(player, false, "Invalid bounty ID.", eco);
            return;
        }

        String error = BountyHuntManager.acceptBounty(player.getUUID(), bountyId);
        if (error != null) {
            sendResult(player, false, error, eco);
        } else {
            BountyHuntManager.saveToDisk(level);
            sendResult(player, true, "Bounty accepted! Hunt the target mob.", eco);
        }
    }

    private static void handleHuntAbandon(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        int bountyId;
        try {
            bountyId = Integer.parseInt(jsonData.trim());
        } catch (NumberFormatException e) {
            sendResult(player, false, "Invalid bounty ID.", eco);
            return;
        }

        String error = BountyHuntManager.abandonBounty(player.getUUID(), bountyId);
        if (error != null) {
            sendResult(player, false, error, eco);
        } else {
            BountyHuntManager.saveToDisk(level);
            sendResult(player, true, "Bounty abandoned.", eco);
        }
    }

    // --- Data response ---

    private static void sendBountyData(ServerPlayer player, ServerLevel level, EconomyManager eco) {
        UUID playerUuid = player.getUUID();
        long now = System.currentTimeMillis();

        JsonObject root = new JsonObject();

        // All active (unfulfilled) bounties
        JsonArray bountiesArr = new JsonArray();
        synchronized (bounties) {
            for (Bounty b : bounties) {
                if (b.fulfilled) continue; // Only show active bounties in browse
                JsonObject obj = new JsonObject();
                obj.addProperty("id", b.id);
                obj.addProperty("posterName", b.posterName);
                obj.addProperty("itemId", b.itemId);
                obj.addProperty("itemName", b.itemName);
                obj.addProperty("quantity", b.quantity);
                obj.addProperty("price", b.priceOffered);
                obj.addProperty("timeAgo", formatTimeAgo(now - b.postedTime));
                obj.addProperty("isOwn", b.posterUuid.equals(playerUuid));
                bountiesArr.add(obj);
            }
        }
        root.add("bounties", bountiesArr);

        // Player's own bounties (active + fulfilled awaiting collection)
        JsonArray myArr = new JsonArray();
        synchronized (bounties) {
            for (Bounty b : bounties) {
                if (!b.posterUuid.equals(playerUuid)) continue;
                JsonObject obj = new JsonObject();
                obj.addProperty("id", b.id);
                obj.addProperty("itemName", b.itemName);
                obj.addProperty("quantity", b.quantity);
                obj.addProperty("price", b.priceOffered);
                obj.addProperty("fulfilled", b.fulfilled);
                obj.addProperty("fulfillerName", b.fulfillerName);
                myArr.add(obj);
            }
        }
        root.add("myBounties", myArr);

        sendResponse(player, "bounty_data", root.toString(), eco);
    }

    private static void sendResult(ServerPlayer player, boolean success, String message, EconomyManager eco) {
        JsonObject obj = new JsonObject();
        obj.addProperty("success", success);
        obj.addProperty("message", message);
        sendResponse(player, "bounty_result", obj.toString(), eco);
    }

    private static void sendResponse(ServerPlayer player, String type, String json, EconomyManager eco) {
        int wallet = eco.getWallet(player.getUUID());
        int bank = eco.getBank(player.getUUID());
        PacketDistributor.sendToPlayer(player, new ComputerDataPayload(type, json, wallet, bank));
    }

    // --- Helpers ---

    private static Bounty findBountyById(int id) {
        synchronized (bounties) {
            for (Bounty b : bounties) {
                if (b.id == id) return b;
            }
        }
        return null;
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

    private static String formatTimeAgo(long elapsedMs) {
        long seconds = elapsedMs / 1000;
        if (seconds < 60) return seconds + "s ago";
        long minutes = seconds / 60;
        if (minutes < 60) return minutes + "m ago";
        long hours = minutes / 60;
        if (hours < 24) return hours + "h ago";
        long days = hours / 24;
        return days + "d ago";
    }

    /**
     * Returns the minimum price per item for the given item ID.
     */
    public static int getMinPricePerItem(String itemId) {
        return MIN_PRICES.getOrDefault(itemId, DEFAULT_MIN_PRICE);
    }

    /**
     * Returns unmodifiable view of all bounties. Call ensureLoaded first on server side.
     */
    public static List<Bounty> getAllBounties() {
        return Collections.unmodifiableList(bounties);
    }

    /**
     * Admin removal of a bounty by ID with escrow refund.
     */
    public static boolean adminRemoveBounty(int bountyId, ServerLevel level, EconomyManager eco) {
        ensureLoaded(level);
        Iterator<Bounty> it = bounties.iterator();
        while (it.hasNext()) {
            Bounty b = it.next();
            if (b.id == bountyId) {
                if (!b.fulfilled) {
                    // Refund escrow to poster's bank
                    int currentBank = eco.getBank(b.posterUuid);
                    eco.setBank(b.posterUuid, currentBank + b.priceOffered);
                }
                it.remove();
                dirty = true;
                saveToDisk(level);
                return true;
            }
        }
        return false;
    }

    /**
     * Ensure bounty data is loaded (for admin use).
     */
    public static void adminEnsureLoaded(ServerLevel level) {
        ensureLoaded(level);
    }

    private static void cleanExpiredBounties(ServerLevel level, EconomyManager eco) {
        long now = System.currentTimeMillis();
        boolean changed = false;
        synchronized (bounties) {
            Iterator<Bounty> it = bounties.iterator();
            while (it.hasNext()) {
                Bounty b = it.next();
                if (!b.fulfilled && (now - b.postedTime) > EXPIRY_MS) {
                    // Refund poster (synchronized via eco's own synchronization)
                    synchronized (eco) {
                        int currentBank = eco.getBank(b.posterUuid);
                        eco.setBank(b.posterUuid, currentBank + b.priceOffered);
                    }
                    it.remove();
                    changed = true;

                    // Notify poster if online
                    ServerPlayer poster = level.getServer().getPlayerList().getPlayer(b.posterUuid);
                    if (poster != null) {
                        poster.sendSystemMessage(Component.literal("Your bounty for " + b.quantity + "x " +
                                b.itemName + " expired. " + b.priceOffered + " MC refunded to bank.").withStyle(ChatFormatting.YELLOW));
                    }
                }
            }
        }
        if (changed) {
            dirty = true;
            saveToDisk(level);
        }
    }

    // --- NbtIo Persistence ---

    private static void ensureLoaded(ServerLevel level) {
        if (!loaded) {
            loadFromDisk(level);
            loaded = true;
        }
    }

    public static void loadFromDisk(ServerLevel level) {
        bounties.clear();
        nextBountyId = 1;
        try {
            File saveDir = level.getServer().getWorldPath(LevelResource.ROOT).toFile();
            File dataFile = new File(saveDir, "data" + File.separator + FILE_NAME);
            if (dataFile.exists()) {
                CompoundTag root = NbtIo.readCompressed((Path) dataFile.toPath(), (NbtAccounter) NbtAccounter.unlimitedHeap());

                nextBountyId = root.getIntOr("nextBountyId", 1);

                ListTag list = root.getListOrEmpty("bounties");
                for (int i = 0; i < list.size(); i++) {
                    CompoundTag tag = list.getCompoundOrEmpty(i);
                    String posterUuidStr = tag.getStringOr("posterUuid", "");
                    if (posterUuidStr.isEmpty()) continue;

                    UUID posterUuid;
                    try {
                        posterUuid = UUID.fromString(posterUuidStr);
                    } catch (IllegalArgumentException e) {
                        continue;
                    }

                    Bounty b = new Bounty(
                            tag.getIntOr("id", 0),
                            posterUuid,
                            tag.getStringOr("posterName", "Unknown"),
                            tag.getStringOr("itemId", ""),
                            tag.getStringOr("itemName", ""),
                            tag.getIntOr("quantity", 1),
                            tag.getIntOr("priceOffered", 0),
                            tag.getLongOr("postedTime", 0L)
                    );
                    b.fulfilled = tag.getBooleanOr("fulfilled", false);
                    String fulfillerStr = tag.getStringOr("fulfillerUuid", "");
                    if (!fulfillerStr.isEmpty()) {
                        try {
                            b.fulfillerUuid = UUID.fromString(fulfillerStr);
                        } catch (IllegalArgumentException e) {
                            // skip
                        }
                    }
                    b.fulfillerName = tag.getStringOr("fulfillerName", "");

                    bounties.add(b);
                }
            }
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to load bounty board data", e);
        }
        dirty = false;
    }

    public static void saveToDisk(ServerLevel level) {
        if (!dirty) return;
        try {
            File saveDir = level.getServer().getWorldPath(LevelResource.ROOT).toFile();
            File dataDir = new File(saveDir, "data");
            if (!dataDir.exists()) {
                dataDir.mkdirs();
            }
            File dataFile = new File(dataDir, FILE_NAME);

            CompoundTag root = new CompoundTag();
            root.putInt("nextBountyId", nextBountyId);

            ListTag list = new ListTag();
            for (Bounty b : bounties) {
                CompoundTag tag = new CompoundTag();
                tag.putInt("id", b.id);
                tag.putString("posterUuid", b.posterUuid.toString());
                tag.putString("posterName", b.posterName);
                tag.putString("itemId", b.itemId);
                tag.putString("itemName", b.itemName);
                tag.putInt("quantity", b.quantity);
                tag.putInt("priceOffered", b.priceOffered);
                tag.putLong("postedTime", b.postedTime);
                tag.putBoolean("fulfilled", b.fulfilled);
                tag.putString("fulfillerUuid", b.fulfillerUuid != null ? b.fulfillerUuid.toString() : "");
                tag.putString("fulfillerName", b.fulfillerName);
                list.add((Tag) tag);
            }
            root.put("bounties", (Tag) list);

            NbtIo.writeCompressed((CompoundTag) root, (Path) dataFile.toPath());
            dirty = false;
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to save bounty board data", e);
        }
    }

    /**
     * Force save regardless of dirty flag. Called on server stop.
     */
    public static void forceSave(ServerLevel level) {
        if (!loaded) return;
        dirty = true;
        saveToDisk(level);
    }

    public static void reset() {
        bounties.clear();
        nextBountyId = 1;
        dirty = false;
        loaded = false;
    }
}
