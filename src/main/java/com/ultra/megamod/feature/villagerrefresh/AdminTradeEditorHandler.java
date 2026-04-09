package com.ultra.megamod.feature.villagerrefresh;

import com.ultra.megamod.feature.computer.admin.AdminSystem;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerData;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.npc.villager.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Server handler for admin trade editing.
 * All trades are sourced from VillagerTrades.TRADES to ensure they're vanilla-valid.
 */
public class AdminTradeEditorHandler {

    private static final int MAX_SEEK_ATTEMPTS = 500;

    public static void handle(ServerPlayer player, AdminTradeEditPayload payload) {
        if (!AdminSystem.isAdmin(player)) return;

        Entity entity = player.level().getEntity(payload.villagerEntityId());
        if (!(entity instanceof Villager villager)) return;
        if (player.distanceTo(villager) > 10.0) return;

        if (villager.getVillagerData().profession().is(VillagerProfession.NONE)
                || villager.getVillagerData().profession().is(VillagerProfession.NITWIT)) {
            player.sendSystemMessage(Component.literal("Cannot edit trades on unemployed/nitwit villagers.").withStyle(ChatFormatting.RED));
            return;
        }

        switch (payload.action()) {
            case AdminTradeEditPayload.ACTION_REROLL_SINGLE -> handleRerollSingle(player, villager, payload.tradeIndex());
            case AdminTradeEditPayload.ACTION_REROLL_ALL -> handleRerollAll(player, villager, payload.lockedMask());
            case AdminTradeEditPayload.ACTION_SET_LEVEL -> handleSetLevel(player, villager, payload.data());
            case AdminTradeEditPayload.ACTION_ADD_TRADE -> handleAddTrade(player, villager);
            case AdminTradeEditPayload.ACTION_SEEK_TRADE -> handleSeekTrade(player, villager, payload.tradeIndex(), payload.searchTerm());
            case AdminTradeEditPayload.ACTION_CREATE_CUSTOM -> handleCreateCustom(player, villager, payload.searchTerm());
        }

        // Send updated offers back to client
        sendOffersToClient(player, villager);
    }

    private static void sendOffersToClient(ServerPlayer player, Villager villager) {
        MerchantOffers offers = villager.getOffers();
        int level = villager.getVillagerData().level();
        PacketDistributor.sendToPlayer(player, new AdminTradeOffersPayload(villager.getId(), level, offers));
    }

    // ======================== SEEK TRADE ========================

    private static void handleSeekTrade(ServerPlayer player, Villager villager, int tradeIndex, String searchTerm) {
        if (searchTerm == null || searchTerm.isBlank()) {
            player.sendSystemMessage(Component.literal("Enter a search term first.").withStyle(ChatFormatting.RED));
            return;
        }

        MerchantOffers offers = villager.getOffers();
        if (tradeIndex < 0 || tradeIndex >= offers.size()) return;

        int tradeLevel = getTradeLevel(villager, tradeIndex);
        VillagerTrades.ItemListing[] listings = getListingsForLevel(villager, tradeLevel);
        if (listings == null || listings.length == 0) {
            player.sendSystemMessage(Component.literal("No trade pool for this level.").withStyle(ChatFormatting.RED));
            return;
        }

        ServerLevel level = (ServerLevel) villager.level();
        String search = searchTerm.toLowerCase().trim();

        // Check if current trade already matches
        if (offerMatchesSearch(offers.get(tradeIndex), search)) {
            player.sendSystemMessage(Component.literal("Trade #" + (tradeIndex + 1) + " already has '" + searchTerm + "'!").withStyle(ChatFormatting.YELLOW));
            return;
        }

        // Reroll until we find a match
        for (int attempt = 1; attempt <= MAX_SEEK_ATTEMPTS; attempt++) {
            int idx = villager.getRandom().nextInt(listings.length);
            MerchantOffer newOffer = listings[idx].getOffer(level, villager, villager.getRandom());
            if (newOffer != null && offerMatchesSearch(newOffer, search)) {
                offers.set(tradeIndex, newOffer);
                level.playSound(null, villager.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.NEUTRAL, 0.6f, 1.4f);
                level.sendParticles(ParticleTypes.HAPPY_VILLAGER, villager.getX(), villager.getY() + 1.0, villager.getZ(), 15, 0.3, 0.5, 0.3, 0.0);
                player.sendSystemMessage(Component.literal("Found '" + searchTerm + "' in trade #" + (tradeIndex + 1) + " after " + attempt + " attempts!").withStyle(ChatFormatting.GREEN));
                return;
            }
        }

        // Not found after max attempts
        level.playSound(null, villager.blockPosition(), SoundEvents.VILLAGER_NO, SoundSource.NEUTRAL, 0.8f, 1.0f);
        player.sendSystemMessage(Component.literal("'" + searchTerm + "' not found after " + MAX_SEEK_ATTEMPTS + " attempts. Try a different search or trade level.").withStyle(ChatFormatting.RED));
    }

    /**
     * Check if a trade offer's result item matches the search term.
     * Checks: item name, enchantment names (for enchanted books), and registry path.
     */
    private static boolean offerMatchesSearch(MerchantOffer offer, String searchLower) {
        ItemStack result = offer.getResult();

        // Check item display name
        String displayName = result.getHoverName().getString().toLowerCase();
        if (displayName.contains(searchLower)) return true;

        // Check item registry name (e.g., "diamond_sword")
        String registryName = result.getItem().builtInRegistryHolder().unwrapKey()
                .map(k -> k.identifier().getPath()).orElse("");
        if (registryName.contains(searchLower)) return true;

        // Check stored enchantments (enchanted books)
        ItemEnchantments storedEnchants = result.get(DataComponents.STORED_ENCHANTMENTS);
        if (storedEnchants != null) {
            for (Holder<Enchantment> enchHolder : storedEnchants.keySet()) {
                // Check enchantment registry path (e.g., "mending", "fortune", "looting")
                String enchPath = enchHolder.unwrapKey()
                        .map(k -> k.identifier().getPath()).orElse("");
                if (enchPath.contains(searchLower)) return true;

                // Check enchantment display name via description
                String enchName = Enchantment.getFullname(enchHolder, storedEnchants.getLevel(enchHolder))
                        .getString().toLowerCase();
                if (enchName.contains(searchLower)) return true;
            }
        }

        // Check regular enchantments on the item
        ItemEnchantments enchants = result.get(DataComponents.ENCHANTMENTS);
        if (enchants != null) {
            for (Holder<Enchantment> enchHolder : enchants.keySet()) {
                String enchPath = enchHolder.unwrapKey()
                        .map(k -> k.identifier().getPath()).orElse("");
                if (enchPath.contains(searchLower)) return true;

                String enchName = Enchantment.getFullname(enchHolder, enchants.getLevel(enchHolder))
                        .getString().toLowerCase();
                if (enchName.contains(searchLower)) return true;
            }
        }

        // Also check cost items (for searching by input, e.g., "emerald")
        String costAName = offer.getBaseCostA().getHoverName().getString().toLowerCase();
        if (costAName.contains(searchLower)) return true;

        return false;
    }

    // ======================== EXISTING HANDLERS ========================

    private static void handleRerollSingle(ServerPlayer player, Villager villager, int tradeIndex) {
        MerchantOffers offers = villager.getOffers();
        if (tradeIndex < 0 || tradeIndex >= offers.size()) return;

        int tradeLevel = getTradeLevel(villager, tradeIndex);
        VillagerTrades.ItemListing[] listings = getListingsForLevel(villager, tradeLevel);
        if (listings == null || listings.length == 0) return;

        ServerLevel level = (ServerLevel) villager.level();
        for (int attempt = 0; attempt < 10; attempt++) {
            int idx = villager.getRandom().nextInt(listings.length);
            MerchantOffer newOffer = listings[idx].getOffer(level, villager, villager.getRandom());
            if (newOffer != null) {
                offers.set(tradeIndex, newOffer);
                break;
            }
        }

        level.playSound(null, villager.blockPosition(), SoundEvents.VILLAGER_WORK_CARTOGRAPHER, SoundSource.NEUTRAL, 0.8f, 1.2f);
        player.sendSystemMessage(Component.literal("Trade #" + (tradeIndex + 1) + " rerolled!").withStyle(ChatFormatting.GREEN));
    }

    private static void handleRerollAll(ServerPlayer player, Villager villager, long lockedMask) {
        MerchantOffers offers = villager.getOffers();
        ServerLevel level = (ServerLevel) villager.level();
        int currentLevel = villager.getVillagerData().level();

        ResourceKey<?> professionKey = villager.getVillagerData().profession().unwrapKey().orElse(null);
        if (professionKey == null) return;

        @SuppressWarnings("unchecked")
        Int2ObjectMap<VillagerTrades.ItemListing[]> tradeMap =
                (Int2ObjectMap<VillagerTrades.ItemListing[]>) VillagerTrades.TRADES.get(professionKey);
        if (tradeMap == null || tradeMap.isEmpty()) return;

        List<MerchantOffer> lockedTrades = new ArrayList<>();
        List<Integer> lockedPositions = new ArrayList<>();
        for (int i = 0; i < offers.size(); i++) {
            if ((lockedMask & (1L << i)) != 0) {
                lockedTrades.add(offers.get(i));
                lockedPositions.add(i);
            }
        }

        offers.clear();
        for (int lvl = 1; lvl <= currentLevel; lvl++) {
            VillagerTrades.ItemListing[] listings = tradeMap.get(lvl);
            if (listings == null) continue;
            addRandomOffers(level, villager, offers, listings, 2);
        }

        for (int i = 0; i < lockedTrades.size(); i++) {
            int pos = lockedPositions.get(i);
            if (pos < offers.size()) {
                offers.set(pos, lockedTrades.get(i));
            } else {
                offers.add(lockedTrades.get(i));
            }
        }

        level.playSound(null, villager.blockPosition(), SoundEvents.ANVIL_USE, SoundSource.NEUTRAL, 1.0f, 1.2f);
        level.sendParticles(ParticleTypes.COMPOSTER, villager.getX(), villager.getY() + 1.0, villager.getZ(), 20, 0.4, 0.6, 0.4, 0.0);
        int lockedCount = Long.bitCount(lockedMask);
        String msg = lockedCount > 0
                ? "All trades rerolled! (" + lockedCount + " locked trades preserved)"
                : "All trades rerolled!";
        player.sendSystemMessage(Component.literal(msg).withStyle(ChatFormatting.GOLD));
    }

    private static void handleSetLevel(ServerPlayer player, Villager villager, int newLevel) {
        newLevel = Math.max(1, Math.min(5, newLevel));
        int oldLevel = villager.getVillagerData().level();

        VillagerData old = villager.getVillagerData();
        VillagerData data = new VillagerData(old.type(), old.profession(), newLevel);
        villager.setVillagerData(data);

        ServerLevel level = (ServerLevel) villager.level();
        ResourceKey<?> professionKey = villager.getVillagerData().profession().unwrapKey().orElse(null);
        if (professionKey == null) return;

        @SuppressWarnings("unchecked")
        Int2ObjectMap<VillagerTrades.ItemListing[]> tradeMap =
                (Int2ObjectMap<VillagerTrades.ItemListing[]>) VillagerTrades.TRADES.get(professionKey);
        if (tradeMap == null) return;

        MerchantOffers offers = villager.getOffers();
        offers.clear();
        for (int lvl = 1; lvl <= newLevel; lvl++) {
            VillagerTrades.ItemListing[] listings = tradeMap.get(lvl);
            if (listings == null) continue;
            addRandomOffers(level, villager, offers, listings, 2);
        }

        level.playSound(null, villager.blockPosition(), SoundEvents.VILLAGER_CELEBRATE, SoundSource.NEUTRAL, 1.0f, 1.0f);
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, villager.getX(), villager.getY() + 1.0, villager.getZ(), 15, 0.3, 0.5, 0.3, 0.0);
        player.sendSystemMessage(Component.literal("Villager level: " + oldLevel + " → " + newLevel + " (trades regenerated)").withStyle(ChatFormatting.GREEN));
    }

    private static void handleAddTrade(ServerPlayer player, Villager villager) {
        int currentLevel = villager.getVillagerData().level();
        VillagerTrades.ItemListing[] listings = getListingsForLevel(villager, currentLevel);
        if (listings == null || listings.length == 0) return;

        ServerLevel level = (ServerLevel) villager.level();
        MerchantOffers offers = villager.getOffers();

        if (offers.size() >= 16) {
            player.sendSystemMessage(Component.literal("Trade list is full (max 16).").withStyle(ChatFormatting.RED));
            return;
        }

        for (int attempt = 0; attempt < 10; attempt++) {
            int idx = villager.getRandom().nextInt(listings.length);
            MerchantOffer newOffer = listings[idx].getOffer(level, villager, villager.getRandom());
            if (newOffer != null) {
                offers.add(newOffer);
                level.playSound(null, villager.blockPosition(), SoundEvents.VILLAGER_YES, SoundSource.NEUTRAL, 0.8f, 1.0f);
                player.sendSystemMessage(Component.literal("Added new trade from level " + currentLevel + " pool!").withStyle(ChatFormatting.GREEN));
                return;
            }
        }
        player.sendSystemMessage(Component.literal("Failed to generate a trade. Try again.").withStyle(ChatFormatting.YELLOW));
    }

    // ======================== CREATE CUSTOM TRADE ========================

    /**
     * Creates a custom trade from a string descriptor.
     * Format: "inputId:inputCount[+input2Id:input2Count]>outputId:outputCount[:maxUses]"
     * Examples:
     *   "minecraft:emerald:10>minecraft:diamond:1"
     *   "minecraft:emerald:32+minecraft:diamond:4>minecraft:netherite_ingot:1:3"
     */
    private static void handleCreateCustom(ServerPlayer player, Villager villager, String descriptor) {
        if (descriptor == null || descriptor.isBlank()) {
            player.sendSystemMessage(Component.literal("Enter a trade in the format: input:count>output:count").withStyle(ChatFormatting.RED));
            return;
        }

        MerchantOffers offers = villager.getOffers();
        if (offers.size() >= 16) {
            player.sendSystemMessage(Component.literal("Trade list is full (max 16).").withStyle(ChatFormatting.RED));
            return;
        }

        try {
            // Split input and output
            String[] halves = descriptor.split(">");
            if (halves.length != 2) {
                player.sendSystemMessage(Component.literal("Invalid format. Use: input:count>output:count").withStyle(ChatFormatting.RED));
                return;
            }

            // Parse inputs (may have two separated by +)
            String[] inputParts = halves[0].split("\\+");
            ItemStack costA = parseItemStack(inputParts[0].trim());
            ItemStack costB = inputParts.length > 1 ? parseItemStack(inputParts[1].trim()) : ItemStack.EMPTY;

            // Parse output (may have optional maxUses at the end)
            String outputStr = halves[1].trim();
            int maxUses = 16; // default
            // Check if last segment is a plain number (maxUses)
            String[] outSegments = outputStr.split(":");
            if (outSegments.length >= 4) {
                try {
                    maxUses = Integer.parseInt(outSegments[outSegments.length - 1]);
                    // Rebuild output string without the maxUses
                    outputStr = String.join(":", java.util.Arrays.copyOf(outSegments, outSegments.length - 1));
                } catch (NumberFormatException ignored) {}
            }
            ItemStack result = parseItemStack(outputStr);

            if (costA.isEmpty() || result.isEmpty()) {
                player.sendSystemMessage(Component.literal("Invalid items. Use registry names like minecraft:emerald:10").withStyle(ChatFormatting.RED));
                return;
            }

            maxUses = Math.max(1, Math.min(maxUses, 9999));
            net.minecraft.world.item.trading.ItemCost costAItem = new net.minecraft.world.item.trading.ItemCost(costA.getItem(), costA.getCount());
            java.util.Optional<net.minecraft.world.item.trading.ItemCost> costBItem = costB.isEmpty()
                    ? java.util.Optional.empty()
                    : java.util.Optional.of(new net.minecraft.world.item.trading.ItemCost(costB.getItem(), costB.getCount()));
            MerchantOffer customOffer = new MerchantOffer(costAItem, costBItem, result, 0, maxUses, 1, 0.05f);
            offers.add(customOffer);

            ServerLevel level = (ServerLevel) villager.level();
            level.playSound(null, villager.blockPosition(), SoundEvents.VILLAGER_YES, SoundSource.NEUTRAL, 0.8f, 1.2f);
            level.sendParticles(ParticleTypes.HAPPY_VILLAGER, villager.getX(), villager.getY() + 1.0, villager.getZ(), 10, 0.3, 0.5, 0.3, 0.0);

            String msg = "Custom trade added: " + costA.getCount() + "x " + costA.getHoverName().getString();
            if (!costB.isEmpty()) msg += " + " + costB.getCount() + "x " + costB.getHoverName().getString();
            msg += " → " + result.getCount() + "x " + result.getHoverName().getString() + " (max " + maxUses + " uses)";
            player.sendSystemMessage(Component.literal(msg).withStyle(ChatFormatting.GREEN));
        } catch (Exception e) {
            player.sendSystemMessage(Component.literal("Error creating trade: " + e.getMessage()).withStyle(ChatFormatting.RED));
        }
    }

    /**
     * Parse "namespace:item_id:count" into an ItemStack.
     * Count defaults to 1 if not specified.
     */
    private static ItemStack parseItemStack(String str) {
        String[] parts = str.split(":");
        if (parts.length < 2) return ItemStack.EMPTY;

        String namespace = parts[0];
        String path = parts[1];
        int count = 1;

        if (parts.length >= 3) {
            try {
                count = Integer.parseInt(parts[2]);
            } catch (NumberFormatException ignored) {
                // Part 2 might be a sub-path, not a count (e.g., "minecraft:oak_planks")
            }
        }

        net.minecraft.resources.Identifier itemId = net.minecraft.resources.Identifier.fromNamespaceAndPath(namespace, path);
        net.minecraft.world.item.Item item = net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(itemId);
        if (item == null || item == net.minecraft.world.item.Items.AIR) return ItemStack.EMPTY;

        count = Math.max(1, Math.min(count, 64));
        return new ItemStack(item, count);
    }

    // ======================== HELPERS ========================

    private static int getTradeLevel(Villager villager, int tradeIndex) {
        int level = 1 + (tradeIndex / 2);
        return Math.min(level, villager.getVillagerData().level());
    }

    private static VillagerTrades.ItemListing[] getListingsForLevel(Villager villager, int level) {
        ResourceKey<?> professionKey = villager.getVillagerData().profession().unwrapKey().orElse(null);
        if (professionKey == null) return null;

        @SuppressWarnings("unchecked")
        Int2ObjectMap<VillagerTrades.ItemListing[]> tradeMap =
                (Int2ObjectMap<VillagerTrades.ItemListing[]>) VillagerTrades.TRADES.get(professionKey);
        if (tradeMap == null) return null;

        return tradeMap.get(level);
    }

    private static void addRandomOffers(ServerLevel level, Villager villager, MerchantOffers offers, VillagerTrades.ItemListing[] listings, int count) {
        ArrayList<VillagerTrades.ItemListing> available = new ArrayList<>(Arrays.asList(listings));
        int added = 0;
        while (added < count && !available.isEmpty()) {
            int index = villager.getRandom().nextInt(available.size());
            MerchantOffer offer = available.remove(index).getOffer(level, villager, villager.getRandom());
            if (offer != null) {
                offers.add(offer);
                added++;
            }
        }
    }
}
