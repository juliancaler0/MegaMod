package com.ultra.megamod.feature.marketplace;

import com.ultra.megamod.MegaMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class MarketplaceManager {

    private static MarketplaceManager INSTANCE;
    private static final String FILE_NAME = "megamod_marketplace.dat";
    private static final long EXPIRY_MS = 48 * 60 * 60 * 1000L; // 48 hours
    private static final int MAX_LISTINGS_PER_PLAYER = 10;
    private static final long INVITE_TIMEOUT_MS = 60 * 1000L; // 60 seconds

    private final List<MarketListing> listings = new ArrayList<>();
    private final Map<UUID, List<MarketNotification>> notifications = new HashMap<>();
    // Trade floor invites keyed by target player UUID
    private final Map<UUID, TradeFloorInvite> pendingInvites = new HashMap<>();
    private int nextListingId = 1;
    private boolean dirty = false;

    // Trade activity log for admin panel (in-memory, not persisted — resets on server restart)
    private static final List<TradeActivity> tradeHistory = Collections.synchronizedList(new ArrayList<>());
    private static final int MAX_TRADE_HISTORY = 200;

    public record TradeActivity(String action, String playerName, String itemName, int quantity, int pricePerUnit, long timestamp) {}

    public static void recordActivity(String action, String playerName, String itemName, int quantity, int pricePerUnit) {
        tradeHistory.add(0, new TradeActivity(action, playerName, itemName, quantity, pricePerUnit, System.currentTimeMillis()));
        if (tradeHistory.size() > MAX_TRADE_HISTORY) {
            tradeHistory.remove(tradeHistory.size() - 1);
        }
    }

    public static List<TradeActivity> getTradeHistory() {
        return Collections.unmodifiableList(tradeHistory);
    }

    // Minimum price per item — copied from BountyBoardHandler
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

    // --- Data model ---

    public enum ListingType { WTS, WTB }

    public static class MarketListing {
        public int id;
        public UUID sellerUuid;
        public String sellerName;
        public ListingType type;
        public String itemId;
        public String itemName;
        public int quantity;
        public int pricePerUnit;
        public long postedTime;
        public boolean active;

        public MarketListing(int id, UUID sellerUuid, String sellerName, ListingType type,
                             String itemId, String itemName, int quantity, int pricePerUnit, long postedTime) {
            this.id = id;
            this.sellerUuid = sellerUuid;
            this.sellerName = sellerName;
            this.type = type;
            this.itemId = itemId;
            this.itemName = itemName;
            this.quantity = quantity;
            this.pricePerUnit = pricePerUnit;
            this.postedTime = postedTime;
            this.active = true;
        }

        public int getTotalPrice() {
            return safeMultiply(pricePerUnit, quantity);
        }
    }

    public static class MarketNotification {
        public UUID fromUuid;
        public String fromName;
        public int listingId;
        public String message;
        public long timestamp;
        public boolean read;

        public MarketNotification(UUID fromUuid, String fromName, int listingId, String message, long timestamp) {
            this.fromUuid = fromUuid;
            this.fromName = fromName;
            this.listingId = listingId;
            this.message = message;
            this.timestamp = timestamp;
            this.read = false;
        }
    }

    // --- Singleton ---

    public static MarketplaceManager get(ServerLevel level) {
        if (INSTANCE == null) {
            INSTANCE = new MarketplaceManager();
            INSTANCE.loadFromDisk(level);
        }
        return INSTANCE;
    }

    public static void reset() {
        INSTANCE = null;
    }

    // --- Public API ---

    public List<MarketListing> getActiveListings(ListingType type) {
        long now = System.currentTimeMillis();
        return listings.stream()
                .filter(l -> l.active && l.type == type && (now - l.postedTime) <= EXPIRY_MS)
                .collect(Collectors.toList());
    }

    public List<MarketListing> getAllActiveListings() {
        long now = System.currentTimeMillis();
        return listings.stream()
                .filter(l -> l.active && (now - l.postedTime) <= EXPIRY_MS)
                .collect(Collectors.toList());
    }

    public List<MarketListing> getPlayerListings(UUID playerUuid) {
        return listings.stream()
                .filter(l -> l.sellerUuid.equals(playerUuid) && l.active)
                .collect(Collectors.toList());
    }

    public MarketListing getListingById(int id) {
        for (MarketListing l : listings) {
            if (l.id == id) return l;
        }
        return null;
    }

    public List<MarketListing> searchListings(String query) {
        long now = System.currentTimeMillis();
        String lowerQuery = query.toLowerCase(Locale.ROOT);
        return listings.stream()
                .filter(l -> l.active && (now - l.postedTime) <= EXPIRY_MS)
                .filter(l -> l.itemName.toLowerCase(Locale.ROOT).contains(lowerQuery)
                        || l.itemId.toLowerCase(Locale.ROOT).contains(lowerQuery))
                .collect(Collectors.toList());
    }

    /**
     * Create a new marketplace listing.
     * @return null on success, error message string on failure
     */
    public String createListing(UUID playerUuid, String playerName, ListingType type,
                                String itemId, String itemName, int quantity, int pricePerUnit,
                                com.ultra.megamod.feature.economy.EconomyManager eco) {
        if (quantity <= 0) return "Quantity must be at least 1.";
        if (pricePerUnit <= 0) return "Price per unit must be at least 1 MC.";

        // Check minimum price
        int minPrice = MIN_PRICES.getOrDefault(itemId, DEFAULT_MIN_PRICE);
        if (pricePerUnit < minPrice) {
            return "Minimum price for this item is " + minPrice + " MC per unit.";
        }

        // Check max listings
        long activeCount = listings.stream()
                .filter(l -> l.sellerUuid.equals(playerUuid) && l.active)
                .count();
        if (activeCount >= MAX_LISTINGS_PER_PLAYER) {
            return "You already have " + MAX_LISTINGS_PER_PLAYER + " active listings.";
        }

        // For WTB listings, escrow coins from bank
        if (type == ListingType.WTB) {
            int totalCost = safeMultiply(pricePerUnit, quantity);
            int bank = eco.getBank(playerUuid);
            if (bank < totalCost) {
                return "Not enough in bank. Need " + totalCost + " MC, have " + bank + " MC.";
            }
            eco.setBank(playerUuid, bank - totalCost);
        }

        MarketListing listing = new MarketListing(nextListingId++, playerUuid, playerName, type,
                itemId, itemName, quantity, pricePerUnit, System.currentTimeMillis());
        listings.add(listing);
        dirty = true;
        recordActivity("Listed " + type.name(), playerName, itemName, quantity, pricePerUnit);
        return null; // success
    }

    /**
     * Cancel a listing. Returns null on success, error message on failure.
     */
    public String cancelListing(UUID playerUuid, int listingId,
                                com.ultra.megamod.feature.economy.EconomyManager eco) {
        MarketListing listing = getListingById(listingId);
        if (listing == null) return "Listing not found.";
        if (!listing.sellerUuid.equals(playerUuid)) return "You can only cancel your own listings.";
        if (!listing.active) return "Listing is already inactive.";

        listing.active = false;
        recordActivity("Cancelled", listing.sellerName, listing.itemName, listing.quantity, listing.pricePerUnit);

        // Refund escrow for WTB
        if (listing.type == ListingType.WTB) {
            int refund = listing.getTotalPrice();
            int currentBank = eco.getBank(playerUuid);
            eco.setBank(playerUuid, currentBank + refund);
        }

        dirty = true;
        return null; // success
    }

    /**
     * Express interest in a listing — creates a notification for the listing owner.
     */
    public void expressInterest(UUID interestedUuid, String interestedName, int listingId) {
        MarketListing listing = getListingById(listingId);
        if (listing == null || !listing.active) return;

        String actionVerb = listing.type == ListingType.WTS ? "buy" : "sell";
        String message = interestedName + " wants to " + actionVerb + " your "
                + listing.quantity + "x " + listing.itemName + "!";

        MarketNotification notif = new MarketNotification(interestedUuid, interestedName,
                listingId, message, System.currentTimeMillis());

        notifications.computeIfAbsent(listing.sellerUuid, k -> new ArrayList<>()).add(notif);

        // Keep only last 20 notifications per player
        List<MarketNotification> playerNotifs = notifications.get(listing.sellerUuid);
        while (playerNotifs.size() > 20) {
            playerNotifs.remove(0);
        }

        dirty = true;
    }

    // --- Trade Floor Invites ---

    public static class TradeFloorInvite {
        public UUID inviterUuid;
        public String inviterName;
        public UUID targetUuid;
        public String targetName;
        public int listingId;
        public long timestamp;

        public TradeFloorInvite(UUID inviterUuid, String inviterName, UUID targetUuid, String targetName, int listingId) {
            this.inviterUuid = inviterUuid;
            this.inviterName = inviterName;
            this.targetUuid = targetUuid;
            this.targetName = targetName;
            this.listingId = listingId;
            this.timestamp = System.currentTimeMillis();
        }

        public boolean isExpired() {
            return (System.currentTimeMillis() - timestamp) > INVITE_TIMEOUT_MS;
        }
    }

    /**
     * Send a trade floor invite to a listing owner.
     * @return null on success, error message on failure
     */
    public String createInvite(UUID inviterUuid, String inviterName, int listingId, ServerLevel level) {
        MarketListing listing = getListingById(listingId);
        if (listing == null || !listing.active) return "Listing not found or no longer active.";
        if (listing.sellerUuid.equals(inviterUuid)) return "You cannot invite yourself.";

        // Check target is online
        ServerPlayer target = level.getServer().getPlayerList().getPlayer(listing.sellerUuid);
        if (target == null) return listing.sellerName + " is offline. They must be online to receive an invite.";

        // Check inviter doesn't already have an outgoing invite to this player
        TradeFloorInvite existing = pendingInvites.get(listing.sellerUuid);
        if (existing != null && !existing.isExpired() && existing.inviterUuid.equals(inviterUuid)) {
            return "You already have a pending invite to " + listing.sellerName + ". Wait for them to respond.";
        }

        // Clean expired invites
        cleanExpiredInvites();

        TradeFloorInvite invite = new TradeFloorInvite(inviterUuid, inviterName, listing.sellerUuid, listing.sellerName, listingId);
        pendingInvites.put(listing.sellerUuid, invite);
        return null; // success
    }

    /**
     * Get the pending invite for a player (as target), or null if none/expired.
     */
    public TradeFloorInvite getPendingInvite(UUID targetUuid) {
        TradeFloorInvite invite = pendingInvites.get(targetUuid);
        if (invite != null && invite.isExpired()) {
            pendingInvites.remove(targetUuid);
            return null;
        }
        return invite;
    }

    /**
     * Accept a pending invite. Returns the invite on success, null if expired/missing.
     */
    public TradeFloorInvite acceptInvite(UUID targetUuid) {
        TradeFloorInvite invite = pendingInvites.remove(targetUuid);
        if (invite == null || invite.isExpired()) return null;
        return invite;
    }

    /**
     * Decline a pending invite.
     */
    public void declineInvite(UUID targetUuid) {
        pendingInvites.remove(targetUuid);
    }

    private void cleanExpiredInvites() {
        pendingInvites.entrySet().removeIf(e -> e.getValue().isExpired());
    }

    public List<MarketNotification> getNotifications(UUID playerUuid) {
        return notifications.getOrDefault(playerUuid, Collections.emptyList());
    }

    public void clearNotifications(UUID playerUuid) {
        List<MarketNotification> notifs = notifications.get(playerUuid);
        if (notifs != null) {
            notifs.removeIf(n -> n.read);
            dirty = true;
        }
    }

    public void markNotificationsRead(UUID playerUuid) {
        List<MarketNotification> notifs = notifications.get(playerUuid);
        if (notifs != null) {
            for (MarketNotification n : notifs) {
                n.read = true;
            }
            dirty = true;
        }
    }

    public int getUnreadNotificationCount(UUID playerUuid) {
        List<MarketNotification> notifs = notifications.get(playerUuid);
        if (notifs == null) return 0;
        return (int) notifs.stream().filter(n -> !n.read).count();
    }

    public static int getMinPricePerItem(String itemId) {
        return MIN_PRICES.getOrDefault(itemId, DEFAULT_MIN_PRICE);
    }

    /**
     * Overflow-safe integer multiplication. Returns Integer.MAX_VALUE on overflow.
     */
    public static int safeMultiply(int a, int b) {
        long result = (long) a * (long) b;
        if (result > Integer.MAX_VALUE) return Integer.MAX_VALUE;
        if (result < Integer.MIN_VALUE) return Integer.MIN_VALUE;
        return (int) result;
    }

    /**
     * Clean up expired listings and refund WTB escrow.
     */
    public void cleanExpired(com.ultra.megamod.feature.economy.EconomyManager eco, ServerLevel level) {
        long now = System.currentTimeMillis();
        boolean changed = false;
        Iterator<MarketListing> it = listings.iterator();
        while (it.hasNext()) {
            MarketListing l = it.next();
            if (l.active && (now - l.postedTime) > EXPIRY_MS) {
                l.active = false;
                recordActivity("Expired", l.sellerName, l.itemName, l.quantity, l.pricePerUnit);

                // Refund WTB escrow
                if (l.type == ListingType.WTB) {
                    int refund = l.getTotalPrice();
                    int currentBank = eco.getBank(l.sellerUuid);
                    eco.setBank(l.sellerUuid, currentBank + refund);

                    // Notify poster if online
                    ServerPlayer poster = level.getServer().getPlayerList().getPlayer(l.sellerUuid);
                    if (poster != null) {
                        poster.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                                "Your market listing for " + l.quantity + "x " + l.itemName
                                        + " expired. " + refund + " MC refunded to bank.")
                                .withStyle(net.minecraft.ChatFormatting.YELLOW));
                    }
                }

                changed = true;
            }
        }

        // Remove old inactive listings (over 72 hours old, to not bloat the file)
        listings.removeIf(l -> !l.active && (now - l.postedTime) > 72 * 60 * 60 * 1000L);

        // Clean old notifications (over 48h)
        for (List<MarketNotification> notifs : notifications.values()) {
            notifs.removeIf(n -> (now - n.timestamp) > 48 * 60 * 60 * 1000L);
        }

        if (changed) {
            dirty = true;
        }
    }

    /**
     * Called when a player disconnects — no special cleanup needed for marketplace
     * since listings persist. But we save if dirty.
     */
    public void onPlayerDisconnect(ServerPlayer player, ServerLevel level) {
        if (dirty) {
            saveToDisk(level);
        }
    }

    // --- Helper: format time remaining ---

    public static String formatTimeRemaining(long postedTime) {
        long elapsed = System.currentTimeMillis() - postedTime;
        long remainingMs = Math.max(0, EXPIRY_MS - elapsed);
        long hours = remainingMs / (60 * 60 * 1000L);
        long minutes = (remainingMs % (60 * 60 * 1000L)) / (60 * 1000L);
        if (hours > 0) {
            return hours + "h " + minutes + "m";
        }
        return minutes + "m";
    }

    // --- NbtIo Persistence ---

    private void loadFromDisk(ServerLevel level) {
        listings.clear();
        notifications.clear();
        nextListingId = 1;

        try {
            File saveDir = level.getServer().getWorldPath(LevelResource.ROOT).toFile();
            File dataFile = new File(saveDir, "data" + File.separator + FILE_NAME);
            if (dataFile.exists()) {
                CompoundTag root = NbtIo.readCompressed((Path) dataFile.toPath(), (NbtAccounter) NbtAccounter.unlimitedHeap());

                nextListingId = root.getIntOr("nextListingId", 1);

                ListTag listingsTag = root.getListOrEmpty("listings");
                for (int i = 0; i < listingsTag.size(); i++) {
                    CompoundTag tag = listingsTag.getCompoundOrEmpty(i);
                    String uuidStr = tag.getStringOr("sellerUuid", "");
                    if (uuidStr.isEmpty()) continue;

                    UUID sellerUuid;
                    try {
                        sellerUuid = UUID.fromString(uuidStr);
                    } catch (IllegalArgumentException e) {
                        continue;
                    }

                    String typeStr = tag.getStringOr("type", "WTS");
                    ListingType type;
                    try {
                        type = ListingType.valueOf(typeStr);
                    } catch (IllegalArgumentException e) {
                        type = ListingType.WTS;
                    }

                    MarketListing listing = new MarketListing(
                            tag.getIntOr("id", 0),
                            sellerUuid,
                            tag.getStringOr("sellerName", "Unknown"),
                            type,
                            tag.getStringOr("itemId", ""),
                            tag.getStringOr("itemName", ""),
                            tag.getIntOr("quantity", 1),
                            tag.getIntOr("pricePerUnit", 1),
                            tag.getLongOr("postedTime", 0L)
                    );
                    listing.active = tag.getBooleanOr("active", true);
                    listings.add(listing);
                }

                // Load notifications
                CompoundTag notifsTag = root.getCompoundOrEmpty("notifications");
                for (String key : notifsTag.keySet()) {
                    UUID playerUuid;
                    try {
                        playerUuid = UUID.fromString(key);
                    } catch (IllegalArgumentException e) {
                        continue;
                    }

                    ListTag notifList = notifsTag.getListOrEmpty(key);
                    List<MarketNotification> playerNotifs = new ArrayList<>();
                    for (int i = 0; i < notifList.size(); i++) {
                        CompoundTag nt = notifList.getCompoundOrEmpty(i);
                        String fromStr = nt.getStringOr("fromUuid", "");
                        if (fromStr.isEmpty()) continue;

                        UUID fromUuid;
                        try {
                            fromUuid = UUID.fromString(fromStr);
                        } catch (IllegalArgumentException e) {
                            continue;
                        }

                        MarketNotification notif = new MarketNotification(
                                fromUuid,
                                nt.getStringOr("fromName", "Unknown"),
                                nt.getIntOr("listingId", 0),
                                nt.getStringOr("message", ""),
                                nt.getLongOr("timestamp", 0L)
                        );
                        notif.read = nt.getBooleanOr("read", false);
                        playerNotifs.add(notif);
                    }
                    if (!playerNotifs.isEmpty()) {
                        notifications.put(playerUuid, playerNotifs);
                    }
                }
            }
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to load marketplace data", e);
        }
        dirty = false;
    }

    public void forceSave(ServerLevel level) {
        dirty = true;
        saveToDisk(level);
    }

    public void saveToDisk(ServerLevel level) {
        if (!dirty) return;
        try {
            File saveDir = level.getServer().getWorldPath(LevelResource.ROOT).toFile();
            File dataDir = new File(saveDir, "data");
            if (!dataDir.exists()) {
                dataDir.mkdirs();
            }
            File dataFile = new File(dataDir, FILE_NAME);

            CompoundTag root = new CompoundTag();
            root.putInt("nextListingId", nextListingId);

            ListTag listingsTag = new ListTag();
            for (MarketListing l : listings) {
                CompoundTag tag = new CompoundTag();
                tag.putInt("id", l.id);
                tag.putString("sellerUuid", l.sellerUuid.toString());
                tag.putString("sellerName", l.sellerName);
                tag.putString("type", l.type.name());
                tag.putString("itemId", l.itemId);
                tag.putString("itemName", l.itemName);
                tag.putInt("quantity", l.quantity);
                tag.putInt("pricePerUnit", l.pricePerUnit);
                tag.putLong("postedTime", l.postedTime);
                tag.putBoolean("active", l.active);
                listingsTag.add((Tag) tag);
            }
            root.put("listings", (Tag) listingsTag);

            // Save notifications
            CompoundTag notifsTag = new CompoundTag();
            for (Map.Entry<UUID, List<MarketNotification>> entry : notifications.entrySet()) {
                ListTag notifList = new ListTag();
                for (MarketNotification n : entry.getValue()) {
                    CompoundTag nt = new CompoundTag();
                    nt.putString("fromUuid", n.fromUuid.toString());
                    nt.putString("fromName", n.fromName);
                    nt.putInt("listingId", n.listingId);
                    nt.putString("message", n.message);
                    nt.putLong("timestamp", n.timestamp);
                    nt.putBoolean("read", n.read);
                    notifList.add((Tag) nt);
                }
                notifsTag.put(entry.getKey().toString(), (Tag) notifList);
            }
            root.put("notifications", (Tag) notifsTag);

            NbtIo.writeCompressed((CompoundTag) root, (Path) dataFile.toPath());
            dirty = false;
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to save marketplace data", e);
        }
    }
}
