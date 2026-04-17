/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.NbtAccounter
 *  net.minecraft.nbt.NbtIo
 *  net.minecraft.resources.Identifier
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.storage.LevelResource
 */
package com.ultra.megamod.feature.economy.shop;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.economy.EconomyManager;
import com.ultra.megamod.feature.economy.shop.ShopItem;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.LevelResource;

public class MegaShop {
    private static MegaShop INSTANCE;
    private static final String FILE_NAME = "megamod_shop.dat";
    public static final Object[][] CATALOG_COMMON;
    public static final Object[][] CATALOG_UNCOMMON;
    public static final Object[][] CATALOG_RARE;
    public static final Object[][] CATALOG_EPIC;
    public static final Object[][] CATALOG_FEATURED_RELIC;
    public static final Object[][] CATALOG_FEATURED_WEAPON;
    /** Admin-only catalog: one legendary weapon available at all times. Not shown to normal players. */
    public static final Object[][] CATALOG_ADMIN_LEGENDARY;
    /** Class items catalog: basic class weapons, armor, jewelry, gems, scrolls, quivers. */
    public static final Object[][] CATALOG_CLASS_ITEMS;
    private static final int DAILY_COMMON_COUNT = 3;
    private static final int DAILY_UNCOMMON_COUNT = 3;
    private static final int DAILY_RARE_COUNT = 2;
    private static final int DAILY_EPIC_COUNT = 1;
    private static final int DAILY_ITEM_COUNT = 9;
    private static final double EPIC_APPEARANCE_CHANCE = 0.25;
    /** Tracks which featured slots each player has bought today. Persisted to prevent exploit on restart. */
    private final Map<String, Set<Integer>> featuredBuys = new ConcurrentHashMap<>();
    private long featuredBuyDay = -1;
    private long lastKnownSeed = 0L;
    private boolean dirty = false;
    private transient ServerLevel cachedLevel = null;
    private int refreshIntervalTicks = 24000;
    private long forceRefreshOffset = 0L;
    private double globalPriceMultiplier = 1.0;
    private double sellPercentage = 0.20;
    private final Map<Integer, ShopItem> manualOverrides = new HashMap<>();

    public static MegaShop get(ServerLevel level) {
        if (INSTANCE == null) {
            INSTANCE = new MegaShop();
            INSTANCE.loadFromDisk(level);
        }
        ServerLevel overworld = level.getServer().overworld();
        MegaShop.INSTANCE.lastKnownSeed = overworld.getSeed();
        MegaShop.INSTANCE.cachedLevel = overworld;
        return INSTANCE;
    }

    public static void reset() {
        INSTANCE = null;
    }

    private void loadFromDisk(ServerLevel level) {
        try {
            File saveDir = level.getServer().getWorldPath(LevelResource.ROOT).toFile();
            File dataFile = new File(saveDir, "data" + File.separator + FILE_NAME);
            if (dataFile.exists()) {
                CompoundTag root = NbtIo.readCompressed((Path)dataFile.toPath(), (NbtAccounter)NbtAccounter.unlimitedHeap());
                this.lastKnownSeed = root.getLongOr("lastKnownSeed", 0L);
                this.refreshIntervalTicks = root.getIntOr("refreshIntervalTicks", 24000);
                if (this.refreshIntervalTicks <= 0) {
                    this.refreshIntervalTicks = 24000;
                }
                this.forceRefreshOffset = root.getLongOr("forceRefreshOffset", 0L);
                this.globalPriceMultiplier = root.getDoubleOr("globalPriceMultiplier", 1.0);
                this.sellPercentage = root.getDoubleOr("sellPercentage", 0.20);
                this.manualOverrides.clear();
                CompoundTag overridesTag = root.getCompoundOrEmpty("manualOverrides");
                for (String key : overridesTag.keySet()) {
                    try {
                        int slot = Integer.parseInt(key);
                        CompoundTag itemTag = overridesTag.getCompoundOrEmpty(key);
                        String itemId = itemTag.getStringOr("itemId", "");
                        String displayName = itemTag.getStringOr("displayName", "");
                        int buyPrice = itemTag.getIntOr("buyPrice", 1);
                        int sellPrice = itemTag.getIntOr("sellPrice", 1);
                        if (!itemId.isEmpty()) {
                            this.manualOverrides.put(slot, new ShopItem(itemId, displayName, buyPrice, sellPrice));
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
                // Load featured buys tracking
                this.featuredBuyDay = root.getLongOr("featuredBuyDay", -1L);
                this.featuredBuys.clear();
                CompoundTag featuredTag = root.getCompoundOrEmpty("featuredBuys");
                long currentDay = System.currentTimeMillis() / 86400000L + this.forceRefreshOffset;
                if (this.featuredBuyDay == currentDay) {
                    for (String key : featuredTag.keySet()) {
                        featuredTag.getIntArray(key).ifPresent(slots -> {
                            Set<Integer> slotSet = ConcurrentHashMap.newKeySet();
                            for (int s : slots) slotSet.add(s);
                            this.featuredBuys.put(key, slotSet);
                        });
                    }
                } else {
                    // Day changed since last save, reset
                    this.featuredBuyDay = currentDay;
                }
            }
        }
        catch (Exception e) {
            MegaMod.LOGGER.error("Failed to load shop data", e);
        }
    }

    public void saveToDisk(ServerLevel level) {
        if (!this.dirty) {
            return;
        }
        try {
            File saveDir = level.getServer().getWorldPath(LevelResource.ROOT).toFile();
            File dataDir = new File(saveDir, "data");
            if (!dataDir.exists()) {
                dataDir.mkdirs();
            }
            File dataFile = new File(dataDir, FILE_NAME);
            CompoundTag root = new CompoundTag();
            root.putLong("lastKnownSeed", this.lastKnownSeed);
            root.putInt("refreshIntervalTicks", this.refreshIntervalTicks);
            root.putLong("forceRefreshOffset", this.forceRefreshOffset);
            root.putDouble("globalPriceMultiplier", this.globalPriceMultiplier);
            root.putDouble("sellPercentage", this.sellPercentage);
            CompoundTag overridesTag = new CompoundTag();
            for (Map.Entry<Integer, ShopItem> entry : this.manualOverrides.entrySet()) {
                CompoundTag itemTag = new CompoundTag();
                ShopItem item = entry.getValue();
                itemTag.putString("itemId", item.itemId());
                itemTag.putString("displayName", item.displayName());
                itemTag.putInt("buyPrice", item.buyPrice());
                itemTag.putInt("sellPrice", item.sellPrice());
                overridesTag.put(String.valueOf(entry.getKey()), (Tag) itemTag);
            }
            root.put("manualOverrides", (Tag) overridesTag);
            // Save featured buys tracking
            root.putLong("featuredBuyDay", this.featuredBuyDay);
            CompoundTag featuredTag = new CompoundTag();
            for (Map.Entry<String, Set<Integer>> entry : this.featuredBuys.entrySet()) {
                int[] slots = entry.getValue().stream().mapToInt(Integer::intValue).toArray();
                featuredTag.putIntArray(entry.getKey(), slots);
            }
            root.put("featuredBuys", (Tag) featuredTag);
            NbtIo.writeCompressed((CompoundTag)root, (Path)dataFile.toPath());
            this.dirty = false;
        }
        catch (Exception e) {
            MegaMod.LOGGER.error("Failed to save shop data", e);
        }
    }

    private void markDirty() {
        this.dirty = true;
    }

    public List<ShopItem> getTodaysItems() {
        if (this.cachedLevel != null) {
            return this.getTodaysItems(this.cachedLevel);
        }
        return List.of();
    }

    public List<ShopItem> getTodaysItems(ServerLevel level) {
        long currentDay = System.currentTimeMillis() / 86400000L + this.forceRefreshOffset;
        long seed = level.getServer().overworld().getSeed();
        Random random = new Random(currentDay + seed);
        ArrayList<ShopItem> items = new ArrayList<ShopItem>();
        this.pickFromTier(CATALOG_COMMON, 3, random, items);
        this.pickFromTier(CATALOG_UNCOMMON, 3, random, items);
        this.pickFromTier(CATALOG_RARE, 2, random, items);
        // Epic items are rare — only ~25% of days have one
        if (random.nextDouble() < EPIC_APPEARANCE_CHANCE) {
            this.pickFromTier(CATALOG_EPIC, 1, random, items);
        } else {
            // Advance random state so featured rotation stays consistent
            this.pickFromTier(CATALOG_EPIC, 1, random, new ArrayList<>());
        }
        // Class items: 2 per day (basic weapons, armor, gems, scrolls)
        this.pickFromTier(CATALOG_CLASS_ITEMS, 2, random, items);
        // Daily Showcase: featured relic + featured weapon (1x purchase limit)
        this.pickFromTier(CATALOG_FEATURED_RELIC, 1, random, items);
        this.pickFromTier(CATALOG_FEATURED_WEAPON, 1, random, items);
        for (Map.Entry<Integer, ShopItem> entry : this.manualOverrides.entrySet()) {
            int slot = entry.getKey();
            if (slot >= 0 && slot < items.size()) {
                items.set(slot, entry.getValue());
            }
        }
        return items;
    }

    private void pickFromTier(Object[][] tierCatalog, int count, Random random, List<ShopItem> result) {
        int i;
        ArrayList<Integer> indices = new ArrayList<Integer>();
        for (i = 0; i < tierCatalog.length; ++i) {
            indices.add(i);
        }
        for (i = indices.size() - 1; i > 0; --i) {
            int j = random.nextInt(i + 1);
            int temp = (Integer)indices.get(i);
            indices.set(i, (Integer)indices.get(j));
            indices.set(j, temp);
        }
        for (i = 0; i < count && i < indices.size(); ++i) {
            int catIdx = (Integer)indices.get(i);
            String itemId = (String)tierCatalog[catIdx][0];
            String displayName = (String)tierCatalog[catIdx][1];
            int basePrice = (Integer)tierCatalog[catIdx][2];
            double variation = 0.8 + random.nextDouble() * 0.4;
            int buyPrice = Math.max(1, (int)Math.round((double)basePrice * variation * this.globalPriceMultiplier));
            int sellPrice = Math.max(1, (int)Math.round((double)buyPrice * this.sellPercentage));
            result.add(new ShopItem(itemId, displayName, buyPrice, sellPrice));
        }
    }

    public String getTodaysItemsJson() {
        if (this.cachedLevel != null) {
            return this.getTodaysItemsJson(this.cachedLevel);
        }
        return "[]";
    }

    public String getTodaysItemsJson(ServerLevel level) {
        List<ShopItem> items = this.getTodaysItems(level);
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < items.size(); ++i) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(items.get(i).toJson());
        }
        sb.append("]");
        return sb.toString();
    }

    public boolean buyItem(ServerPlayer player, int index) {
        ServerLevel level = player.level();
        List<ShopItem> items = this.getTodaysItems(level.getServer().overworld());
        if (index < 0 || index >= items.size()) {
            return false;
        }
        ShopItem item = items.get(index);
        EconomyManager eco = EconomyManager.get(level);
        int finalPrice = applyPrestigeShopDiscount(player, item.buyPrice());
        if (!eco.spendWallet(player.getUUID(), finalPrice)) {
            return false;
        }
        // Featured item: enforce 1x purchase per player per day
        if (isFeaturedItem(item.itemId())) {
            if (hasBoughtFeatured(player.getUUID(), index)) {
                return false;
            }
        }
        ItemStack stack;
        // Featured slot: give the exact item shown with rolled stats
        if (isFeaturedItem(item.itemId())) {
            Identifier featId = Identifier.parse(item.itemId());
            Optional<?> featOpt = BuiltInRegistries.ITEM.getOptional(featId);
            if (featOpt.isPresent()) {
                net.minecraft.world.item.Item baseItem = (net.minecraft.world.item.Item) featOpt.get();
                if (baseItem instanceof com.ultra.megamod.feature.relics.weapons.RpgWeaponItem rpgWeapon) {
                    // RPG weapons: use WeaponStatRoller with minimum RARE quality
                    stack = new ItemStack(baseItem);
                    net.minecraft.util.RandomSource random = net.minecraft.util.RandomSource.create();
                    com.ultra.megamod.feature.relics.data.WeaponRarity rarity = com.ultra.megamod.feature.relics.data.WeaponRarity.roll(random);
                    // Reroll until at least RARE
                    while (rarity.ordinal() < com.ultra.megamod.feature.relics.data.WeaponRarity.RARE.ordinal()) {
                        rarity = com.ultra.megamod.feature.relics.data.WeaponRarity.roll(random);
                    }
                    com.ultra.megamod.feature.relics.data.WeaponStatRoller.rollAndApply(stack, rpgWeapon.getBaseDamage(), rarity, random, rpgWeapon.isShield());
                } else {
                    // Non-weapon featured items: use dungeon loot generator as before
                    stack = com.ultra.megamod.feature.dungeons.loot.DungeonLootGenerator.generateFromBase(
                        baseItem,
                        com.ultra.megamod.feature.dungeons.DungeonTier.NORMAL,
                        net.minecraft.util.RandomSource.create());
                }
            } else {
                stack = ItemStack.EMPTY;
            }
        } else if (item.itemId().equals("minecraft:enchanted_book")) {
            stack = createRandomEnchantedBook(level);
        } else {
            stack = this.createItemStack(item.itemId());
        }
        if (stack.isEmpty()) {
            eco.addWallet(player.getUUID(), finalPrice);
            return false;
        }
        if (!player.getInventory().add(stack)) {
            player.spawnAtLocation(player.level(), stack);
        }
        if (isFeaturedItem(item.itemId())) {
            markFeaturedBought(player.getUUID(), index);
        }
        this.markDirty();
        return true;
    }

    public boolean sellItem(ServerPlayer player, int index) {
        ServerLevel level = player.level();
        List<ShopItem> items = this.getTodaysItems(level.getServer().overworld());
        if (index < 0 || index >= items.size()) {
            return false;
        }
        ShopItem shopItem = items.get(index);
        Identifier itemKey = Identifier.parse((String)shopItem.itemId());
        for (int i = 0; i < player.getInventory().getContainerSize(); ++i) {
            ItemStack invStack = player.getInventory().getItem(i);
            Identifier stackId = BuiltInRegistries.ITEM.getKey(invStack.getItem());
            if (invStack.isEmpty() || !stackId.equals((Object)itemKey)) continue;
            invStack.shrink(1);
            int finalSellPrice = applyPrestigeSellBonus(player, shopItem.sellPrice());
            EconomyManager eco = EconomyManager.get(level);
            eco.addWallet(player.getUUID(), finalSellPrice);
            this.markDirty();
            return true;
        }
        return false;
    }

    /// Each total prestige level grants a 3% shop discount (capped at 50%).
    private static int applyPrestigeShopDiscount(ServerPlayer player, int basePrice) {
        int totalPrestige = com.ultra.megamod.feature.skills.prestige.PrestigeManager
                .get(player.level()).getTotalPrestige(player.getUUID());
        if (totalPrestige <= 0) return basePrice;
        double discount = Math.min(0.5, 0.03 * totalPrestige);
        return Math.max(1, (int) Math.round(basePrice * (1.0 - discount)));
    }

    /// Each total prestige level grants a 5% sell-price bonus (uncapped — matches coin bonus rules).
    private static int applyPrestigeSellBonus(ServerPlayer player, int baseSellPrice) {
        int totalPrestige = com.ultra.megamod.feature.skills.prestige.PrestigeManager
                .get(player.level()).getTotalPrestige(player.getUUID());
        if (totalPrestige <= 0) return baseSellPrice;
        return (int) Math.round(baseSellPrice * (1.0 + 0.05 * totalPrestige));
    }

    public static boolean isEpicItem(String itemId) {
        for (Object[] entry : CATALOG_EPIC) {
            if (entry[0].equals(itemId)) return true;
        }
        return false;
    }

    public boolean hasEpicToday() {
        List<ShopItem> items = getTodaysItems();
        for (ShopItem item : items) {
            if (isEpicItem(item.itemId())) return true;
        }
        return false;
    }

    private static boolean isFeaturedItem(String itemId) {
        for (Object[] entry : CATALOG_FEATURED_RELIC) {
            if (entry[0].equals(itemId)) return true;
        }
        for (Object[] entry : CATALOG_FEATURED_WEAPON) {
            if (entry[0].equals(itemId)) return true;
        }
        return false;
    }

    private boolean isFeaturedSlot(int index) {
        return index >= DAILY_ITEM_COUNT;
    }

    private boolean hasBoughtFeatured(java.util.UUID playerUuid, int slotIndex) {
        long currentDay = System.currentTimeMillis() / 86400000L + this.forceRefreshOffset;
        if (currentDay != featuredBuyDay) {
            featuredBuys.clear();
            featuredBuyDay = currentDay;
        }
        Set<Integer> bought = featuredBuys.get(playerUuid.toString());
        return bought != null && bought.contains(slotIndex);
    }

    private void markFeaturedBought(java.util.UUID playerUuid, int slotIndex) {
        long currentDay = System.currentTimeMillis() / 86400000L + this.forceRefreshOffset;
        if (currentDay != featuredBuyDay) {
            featuredBuys.clear();
            featuredBuyDay = currentDay;
        }
        featuredBuys.computeIfAbsent(playerUuid.toString(), k -> ConcurrentHashMap.newKeySet()).add(slotIndex);
        this.markDirty();
    }

    private ItemStack createItemStack(String itemId) {
        Identifier id = Identifier.parse((String)itemId);
        Optional itemOptional = BuiltInRegistries.ITEM.getOptional(id);
        if (itemOptional.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return new ItemStack((ItemLike)itemOptional.get());
    }

    private static ItemStack createRandomEnchantedBook(ServerLevel level) {
        try {
            Registry<Enchantment> enchReg = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
            List<Holder.Reference<Enchantment>> all = new java.util.ArrayList<>();
            enchReg.listElements().forEach(all::add);
            if (all.isEmpty()) return ItemStack.EMPTY;
            Random random = new Random();
            Holder.Reference<Enchantment> chosen = all.get(random.nextInt(all.size()));
            int maxLevel = chosen.value().getMaxLevel();
            int enchLevel = 1 + random.nextInt(maxLevel);
            ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
            ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
            mutable.set(chosen, enchLevel);
            book.set(DataComponents.STORED_ENCHANTMENTS, mutable.toImmutable());
            return book;
        } catch (Exception e) {
            return ItemStack.EMPTY;
        }
    }

    // --- Admin API methods ---

    public void forceRefresh() {
        this.forceRefreshOffset++;
        this.manualOverrides.clear();
        this.featuredBuys.clear();
        this.markDirty();
    }

    public void setRefreshInterval(int ticks) {
        if (ticks <= 0) {
            ticks = 24000;
        }
        this.refreshIntervalTicks = ticks;
        this.markDirty();
    }

    public void setGlobalPriceMultiplier(double mult) {
        this.globalPriceMultiplier = mult;
        this.markDirty();
    }

    public void setSellPercentage(double pct) {
        if (pct < 0.0) {
            pct = 0.0;
        }
        if (pct > 1.0) {
            pct = 1.0;
        }
        this.sellPercentage = pct;
        this.markDirty();
    }

    public void setManualOverride(int slot, ShopItem item) {
        this.manualOverrides.put(slot, item);
        this.markDirty();
    }

    public void clearManualOverrides() {
        this.manualOverrides.clear();
        this.markDirty();
    }

    public int getRefreshIntervalTicks() {
        return this.refreshIntervalTicks;
    }

    public double getGlobalPriceMultiplier() {
        return this.globalPriceMultiplier;
    }

    public double getSellPercentage() {
        return this.sellPercentage;
    }

    /**
     * Returns the admin-only legendary weapon available today.
     * Rotates daily from CATALOG_ADMIN_LEGENDARY. Only shown to admin players.
     */
    public ShopItem getAdminLegendaryItem(ServerLevel level) {
        if (CATALOG_ADMIN_LEGENDARY.length == 0) return null;
        long currentDay = System.currentTimeMillis() / 86400000L + this.forceRefreshOffset;
        int index = (int) (Math.abs(currentDay) % CATALOG_ADMIN_LEGENDARY.length);
        Object[] entry = CATALOG_ADMIN_LEGENDARY[index];
        String itemId = (String) entry[0];
        String displayName = (String) entry[1];
        int basePrice = (Integer) entry[2];
        int buyPrice = Math.max(1, (int) Math.round((double) basePrice * this.globalPriceMultiplier));
        int sellPrice = Math.max(1, (int) Math.round((double) buyPrice * this.sellPercentage));
        return new ShopItem(itemId, displayName, buyPrice, sellPrice);
    }

    /**
     * Returns the full admin legendary catalog as ShopItems.
     */
    public List<ShopItem> getAdminLegendaryCatalog() {
        ArrayList<ShopItem> items = new ArrayList<>();
        addCatalogTier(CATALOG_ADMIN_LEGENDARY, items);
        return items;
    }

    /**
     * Checks if an item ID is a legendary weapon (from admin catalog).
     */
    public static boolean isAdminLegendary(String itemId) {
        for (Object[] entry : CATALOG_ADMIN_LEGENDARY) {
            if (entry[0].equals(itemId)) return true;
        }
        return false;
    }

    public List<ShopItem> getFullCatalog() {
        ArrayList<ShopItem> all = new ArrayList<ShopItem>();
        this.addCatalogTier(CATALOG_COMMON, all);
        this.addCatalogTier(CATALOG_UNCOMMON, all);
        this.addCatalogTier(CATALOG_CLASS_ITEMS, all);
        this.addCatalogTier(CATALOG_RARE, all);
        this.addCatalogTier(CATALOG_EPIC, all);
        return all;
    }

    private void addCatalogTier(Object[][] tierCatalog, List<ShopItem> result) {
        for (Object[] entry : tierCatalog) {
            String itemId = (String) entry[0];
            String displayName = (String) entry[1];
            int basePrice = (Integer) entry[2];
            int buyPrice = Math.max(1, (int) Math.round((double) basePrice * this.globalPriceMultiplier));
            int sellPrice = Math.max(1, (int) Math.round((double) buyPrice * this.sellPercentage));
            result.add(new ShopItem(itemId, displayName, buyPrice, sellPrice));
        }
    }

    static {
        CATALOG_COMMON = new Object[][]{{"minecraft:cobblestone", "Cobblestone", 1}, {"minecraft:dirt", "Dirt", 1}, {"minecraft:oak_log", "Oak Log", 3}, {"minecraft:birch_log", "Birch Log", 3}, {"minecraft:spruce_log", "Spruce Log", 3}, {"minecraft:sand", "Sand", 2}, {"minecraft:gravel", "Gravel", 2}, {"minecraft:glass", "Glass", 3}, {"minecraft:white_wool", "Wool", 4}, {"minecraft:brick", "Brick", 4}, {"minecraft:stone_bricks", "Stone Bricks", 3}, {"minecraft:arrow", "Arrow", 2}, {"minecraft:bone", "Bone", 2}, {"minecraft:string", "String", 2}, {"minecraft:leather", "Leather", 3}, {"minecraft:paper", "Paper", 2}, {"minecraft:coal", "Coal", 2}};
        CATALOG_UNCOMMON = new Object[][]{{"minecraft:iron_ingot", "Iron Ingot", 12}, {"minecraft:copper_ingot", "Copper Ingot", 8}, {"minecraft:lapis_lazuli", "Lapis Lazuli", 10}, {"minecraft:redstone", "Redstone", 8}, {"minecraft:gold_ingot", "Gold Ingot", 20}, {"minecraft:quartz", "Quartz", 10}, {"minecraft:glowstone_dust", "Glowstone Dust", 12}, {"minecraft:prismarine_shard", "Prismarine Shard", 15}, {"minecraft:gunpowder", "Gunpowder", 10}, {"minecraft:slime_ball", "Slime Ball", 12}, {"minecraft:blaze_rod", "Blaze Rod", 25}, {"minecraft:ender_pearl", "Ender Pearl", 30}, {"minecraft:phantom_membrane", "Phantom Membrane", 15}, {"minecraft:name_tag", "Name Tag", 35}, {"minecraft:saddle", "Saddle", 30}, {"minecraft:book", "Book", 5}, {"minecraft:experience_bottle", "Bottle o' Enchanting", 20}, {"minecraft:honey_bottle", "Honey Bottle", 15}};
        CATALOG_RARE = new Object[][]{{"minecraft:diamond", "Diamond", 80}, {"minecraft:emerald", "Emerald", 60}, {"minecraft:obsidian", "Obsidian", 45}, {"minecraft:enchanted_book", "Enchanted Book", 100}, {"minecraft:golden_apple", "Golden Apple", 50}, {"minecraft:shulker_shell", "Shulker Shell", 120}, {"minecraft:nautilus_shell", "Nautilus Shell", 90}, {"minecraft:heart_of_the_sea", "Heart of the Sea", 150}, {"minecraft:ghast_tear", "Ghast Tear", 65}, {"minecraft:crying_obsidian", "Crying Obsidian", 50}, {"minecraft:lodestone", "Lodestone", 100}, {"minecraft:respawn_anchor", "Respawn Anchor", 130}};
        CATALOG_EPIC = new Object[][]{{"minecraft:ancient_debris", "Ancient Debris", 750},{"minecraft:enchanted_golden_apple", "Enchanted Apple", 1000}, {"minecraft:totem_of_undying", "Totem of Undying", 700}, {"minecraft:conduit", "Conduit", 1100}, {"minecraft:end_crystal", "End Crystal", 550}, {"minecraft:dragon_breath", "Dragon Breath", 500}, {"minecraft:music_disc_pigstep", "Pigstep Disc", 600}, {"minecraft:trident", "Trident", 650}, {"minecraft:wither_skeleton_skull", "Wither Skull", 600}, {"minecraft:heavy_core", "Heavy Core", 900}, {"minecraft:mace", "Mace", 750}};
        // Daily Showcase catalogs: 1 relic + 1 weapon per day, 1x purchase limit
        CATALOG_FEATURED_RELIC = new Object[][]{
            {"megamod:leather_belt", "\u00A7eRelic: Leather Belt", 500},
            {"megamod:wool_mitten", "\u00A7eRelic: Wool Mitten", 500},
            {"megamod:arrow_quiver", "\u00A7eRelic: Arrow Quiver", 500},
            {"megamod:iron_fist", "\u00A7eRelic: Iron Fist", 500},
            {"megamod:bastion_ring", "\u00A7eRelic: Bastion Ring", 500},
            {"megamod:ice_skates", "\u00A7eRelic: Ice Skates", 500},
            {"megamod:roller_skates", "\u00A7eRelic: Roller Skates", 500},
            {"megamod:chorus_inhibitor", "\u00A7eRelic: Chorus Inhibitor", 500},
            {"megamod:reflection_necklace", "\u00A7eRelic: Reflection Necklace", 750},
            {"megamod:stormband", "\u00A7eRelic: Stormband", 750},
            {"megamod:verdant_signet", "\u00A7eRelic: Verdant Signet", 750},
            {"megamod:emberstone_band", "\u00A7eRelic: Emberstone Band", 750},
            {"megamod:lodestone_magnet", "\u00A7eRelic: Lodestone Magnet", 600},
            {"megamod:horse_flute", "\u00A7eRelic: Horse Flute", 500},
            {"megamod:spore_sack", "\u00A7eRelic: Spore Sack", 500},
        };
        // Featured weapons: basic class items only (no legendary/unique weapons for normal players)
        CATALOG_FEATURED_WEAPON = new Object[][]{
            {"megamod:iron_claymore", "\u00A76Featured: Iron Claymore", 60},
            {"megamod:iron_dagger", "\u00A76Featured: Iron Dagger", 45},
            {"megamod:iron_sickle", "\u00A76Featured: Iron Sickle", 45},
            {"megamod:iron_mace", "\u00A76Featured: Iron Mace", 50},
            {"megamod:iron_glaive", "\u00A76Featured: Iron Glaive", 50},
            {"megamod:iron_double_axe", "\u00A76Featured: Iron Double Axe", 60},
            {"megamod:iron_spear", "\u00A76Featured: Iron Spear", 50},
            {"megamod:iron_great_hammer", "\u00A76Featured: Iron Great Hammer", 60},
            {"megamod:wand_arcane", "\u00A76Featured: Arcane Wand", 50},
            {"megamod:wand_fire", "\u00A76Featured: Fire Wand", 50},
            {"megamod:wand_frost", "\u00A76Featured: Frost Wand", 50},
            {"megamod:holy_wand", "\u00A76Featured: Holy Wand", 50},
            {"megamod:composite_longbow", "\u00A76Featured: Composite Longbow", 50},
            {"megamod:iron_kite_shield", "\u00A76Featured: Iron Kite Shield", 50},
            {"megamod:holy_staff", "\u00A76Featured: Holy Staff", 60},
            {"megamod:staff_wizard", "\u00A76Featured: Wizard Staff", 60},
        };
        // Admin-only legendary weapons: always one available via admin shop section
        CATALOG_ADMIN_LEGENDARY = new Object[][]{
            {"megamod:unique_claymore_1", "\u00A7cLegendary: Cataclysm's Edge", 5000},
            {"megamod:unique_dagger_1", "\u00A7cLegendary: Frost Fang", 4000},
            {"megamod:battledancer", "\u00A7cLegendary: Battledancer", 5000},
            {"megamod:whisperwind", "\u00A7cLegendary: Whisperwind", 5000},
            {"megamod:vampiric_tome", "\u00A7cLegendary: Vampiric Tome", 5000},
            {"megamod:briarthorn", "\u00A7cLegendary: Briarthorn", 5000},
            {"megamod:unique_katana_1", "\u00A7cLegendary: Windcutter", 4000},
            {"megamod:unique_hammer_1", "\u00A7cLegendary: Shockwave Hammer", 4000},
            {"megamod:unique_rapier_1", "\u00A7cLegendary: Duelist's Sting", 4000},
            {"megamod:unique_spear_1", "\u00A7cLegendary: Frostbite Spear", 4000},
            {"megamod:unique_whip_1", "\u00A7cLegendary: Serpent's Lash", 4000},
            {"megamod:soka_singing_blade", "\u00A7cLegendary: Soka Singing Blade", 10000},
        };
        // ─── Class Items Catalog: basic weapons, armor, jewelry, gems, scrolls, quivers ───
        CATALOG_CLASS_ITEMS = new Object[][]{
            // Basic class weapons (stone/iron tier): 50-200 coins
            {"megamod:stone_claymore", "Stone Claymore", 80},
            {"megamod:iron_claymore", "Iron Claymore", 150},
            {"megamod:wooden_great_hammer", "Wooden Great Hammer", 60},
            {"megamod:stone_great_hammer", "Stone Great Hammer", 80},
            {"megamod:iron_great_hammer", "Iron Great Hammer", 150},
            {"megamod:golden_mace", "Golden Mace", 70},
            {"megamod:iron_mace", "Iron Mace", 120},
            {"megamod:flint_dagger", "Flint Dagger", 50},
            {"megamod:iron_dagger", "Iron Dagger", 100},
            {"megamod:iron_sickle", "Iron Sickle", 100},
            {"megamod:stone_double_axe", "Stone Double Axe", 80},
            {"megamod:iron_double_axe", "Iron Double Axe", 150},
            {"megamod:iron_glaive", "Iron Glaive", 150},
            {"megamod:flint_spear", "Flint Spear", 50},
            {"megamod:iron_spear", "Iron Spear", 120},
            {"megamod:wand_novice", "Novice Wand", 80},
            {"megamod:acolyte_wand", "Acolyte Wand", 100},
            {"megamod:staff_wizard", "Wizard Staff", 150},
            {"megamod:holy_wand", "Holy Wand", 120},
            {"megamod:holy_staff", "Holy Staff", 150},
            {"megamod:composite_longbow", "Composite Longbow", 180},
            {"megamod:iron_kite_shield", "Iron Kite Shield", 120},
            {"megamod:golden_kite_shield", "Golden Kite Shield", 100},
            // Basic class armor (T1): 100-300 coins
            {"megamod:wizard_robe_head", "Wizard Hood", 100},
            {"megamod:wizard_robe_chest", "Wizard Robe", 200},
            {"megamod:wizard_robe_legs", "Wizard Pants", 150},
            {"megamod:wizard_robe_feet", "Wizard Boots", 100},
            {"megamod:paladin_armor_head", "Paladin Helmet", 150},
            {"megamod:paladin_armor_chest", "Paladin Chestplate", 300},
            {"megamod:paladin_armor_legs", "Paladin Greaves", 250},
            {"megamod:paladin_armor_feet", "Paladin Sabatons", 150},
            {"megamod:rogue_armor_head", "Rogue Hood", 100},
            {"megamod:rogue_armor_chest", "Rogue Tunic", 200},
            {"megamod:rogue_armor_legs", "Rogue Leggings", 150},
            {"megamod:rogue_armor_feet", "Rogue Boots", 100},
            {"megamod:warrior_armor_head", "Warrior Helm", 150},
            {"megamod:warrior_armor_chest", "Warrior Plate", 300},
            {"megamod:warrior_armor_legs", "Warrior Greaves", 250},
            {"megamod:warrior_armor_feet", "Warrior Boots", 150},
            {"megamod:archer_armor_head", "Archer Cap", 100},
            {"megamod:archer_armor_chest", "Archer Vest", 200},
            {"megamod:archer_armor_legs", "Archer Leggings", 150},
            {"megamod:archer_armor_feet", "Archer Boots", 100},
            // Basic jewelry (copper/iron/gold rings): 50-150 coins
            {"megamod:copper_ring", "Copper Ring", 50},
            {"megamod:iron_ring", "Iron Ring", 80},
            {"megamod:gold_ring", "Gold Ring", 120},
            {"megamod:emerald_necklace", "Emerald Necklace", 150},
            {"megamod:diamond_ring", "Diamond Ring", 150},
            {"megamod:diamond_necklace", "Diamond Necklace", 150},
            // Raw gems: 25-50 coins
            {"megamod:ruby", "Ruby", 35},
            {"megamod:topaz", "Topaz", 25},
            {"megamod:citrine", "Citrine", 25},
            {"megamod:jade", "Jade", 30},
            {"megamod:sapphire", "Sapphire", 40},
            {"megamod:tanzanite", "Tanzanite", 50},
            // Spell scrolls: 500-1000 coins
            {"megamod:scroll_fireball", "Scroll: Fireball", 600},
            {"megamod:scroll_frostbolt", "Scroll: Frostbolt", 600},
            {"megamod:scroll_arcane_bolt", "Scroll: Arcane Bolt", 500},
            {"megamod:scroll_heal", "Scroll: Heal", 700},
            {"megamod:scroll_flash_heal", "Scroll: Flash Heal", 800},
            {"megamod:scroll_shadow_step", "Scroll: Shadow Step", 750},
            {"megamod:scroll_power_shot", "Scroll: Power Shot", 700},
            {"megamod:scroll_charge", "Scroll: Charge", 650},
            // Spell books: 2000-5000 coins
            {"megamod:arcane_spell_book", "Arcane Spell Book", 3000},
            {"megamod:fire_spell_book", "Fire Spell Book", 3500},
            {"megamod:frost_spell_book", "Frost Spell Book", 3000},
            {"megamod:healing_spell_book", "Healing Spell Book", 5000},
            // Quivers: 200-500 coins
            {"megamod:small_quiver", "Small Quiver", 200},
        };
    }
}

