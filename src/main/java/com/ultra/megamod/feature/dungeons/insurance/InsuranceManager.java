package com.ultra.megamod.feature.dungeons.insurance;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.computer.network.handlers.PartyHandler;
import com.ultra.megamod.feature.dimensions.DimensionHelper;
import com.ultra.megamod.feature.dimensions.MegaModDimensions;
import com.ultra.megamod.feature.dungeons.DungeonManager;
import com.ultra.megamod.feature.dungeons.DungeonTier;
import com.ultra.megamod.feature.dungeons.network.DungeonSyncPayload;
import com.ultra.megamod.feature.dungeons.insurance.network.InsuranceOpenPayload;
import com.ultra.megamod.feature.dungeons.insurance.network.InsuranceStatusPayload;
import com.ultra.megamod.feature.economy.EconomyManager;
import com.ultra.megamod.feature.relics.accessory.LibAccessoryLookup;
import com.ultra.megamod.feature.relics.data.AccessorySlotType;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.*;

public class InsuranceManager {
    private static final Gson GSON = new Gson();
    private static final long SESSION_TIMEOUT_MS = 60_000; // 60 seconds

    // Pending insurance sessions — keyed by key-holder UUID
    private static final Map<UUID, InsuranceSession> pendingSessions = new HashMap<>();

    // Active insurance for running dungeons — instanceId → (playerUUID → set of insured slot names)
    private static final Map<String, Map<UUID, Set<String>>> activeInsurance = new HashMap<>();

    // Reverse lookup: playerUUID → keyHolder UUID (to find which session a party member belongs to)
    private static final Map<UUID, UUID> playerToSession = new HashMap<>();

    public static void createSession(ServerPlayer keyUser, DungeonTier tier, InteractionHand hand, Set<UUID> partyMembers) {
        UUID keyHolderUUID = keyUser.getUUID();

        // Cancel any existing session for this player
        cancelSession(keyHolderUUID);

        InsuranceSession session = new InsuranceSession(
                tier, keyHolderUUID, hand, partyMembers, System.currentTimeMillis()
        );
        pendingSessions.put(keyHolderUUID, session);

        // Map all party members to this session
        for (UUID memberId : partyMembers) {
            playerToSession.put(memberId, keyHolderUUID);
        }
        // Also map the key holder
        playerToSession.put(keyHolderUUID, keyHolderUUID);

        ServerLevel overworld = keyUser.level().getServer().overworld();

        // Send InsuranceOpenPayload to all involved players
        List<ServerPlayer> allPlayers = new ArrayList<>();
        allPlayers.add(keyUser);
        for (UUID memberId : partyMembers) {
            if (memberId.equals(keyHolderUUID)) continue;
            ServerPlayer member = overworld.getServer().getPlayerList().getPlayer(memberId);
            if (member != null) {
                allPlayers.add(member);
            } else {
                MegaMod.LOGGER.warn("Insurance: party member {} not found on server — skipping", memberId);
            }
        }

        for (ServerPlayer target : allPlayers) {
            // Build per-player slot costs
            JsonObject slotCosts = buildSlotCosts(target, tier, overworld);
            // Build party names list
            JsonArray partyNames = new JsonArray();
            for (ServerPlayer p : allPlayers) {
                partyNames.add(p.getGameProfile().name());
            }

            PacketDistributor.sendToPlayer(target,
                    new InsuranceOpenPayload(tier.getDisplayName(), slotCosts.toString(), partyNames.toString()),
                    new CustomPacketPayload[0]);
        }

        MegaMod.LOGGER.info("Insurance session created for {} ({} tier, {} players)",
                keyUser.getGameProfile().name(), tier.getDisplayName(), allPlayers.size());
    }

    private static JsonObject buildSlotCosts(ServerPlayer player, DungeonTier tier, ServerLevel overworld) {
        JsonObject costs = new JsonObject();

        // Armor slots
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            ItemStack armor = player.getItemBySlot(slot);
            if (!armor.isEmpty()) {
                String key = "armor_" + slot.getName();
                costs.addProperty(key, InsuranceCosts.getCost(armor, tier));
            }
        }

        // Main hand
        ItemStack mainHand = player.getMainHandItem();
        if (!mainHand.isEmpty()) {
            costs.addProperty("mainhand", InsuranceCosts.getCost(mainHand, tier));
        }

        // Offhand
        ItemStack offhand = player.getOffhandItem();
        if (!offhand.isEmpty()) {
            costs.addProperty("offhand", InsuranceCosts.getCost(offhand, tier));
        }

        // Accessory slots
        for (AccessorySlotType slotType : AccessorySlotType.values()) {
            if (slotType == AccessorySlotType.NONE) continue;
            ItemStack accessory = LibAccessoryLookup.getEquipped(player, slotType);
            if (!accessory.isEmpty()) {
                String key = "accessory_" + slotType.name();
                costs.addProperty(key, InsuranceCosts.getCost(accessory, tier));
            }
        }

        // Inventory slots (hotbar 0-8 + main inventory 9-35)
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack invItem = player.getInventory().getItem(i);
            if (!invItem.isEmpty()) {
                String key = "inv_" + i;
                costs.addProperty(key, InsuranceCosts.getCost(invItem, tier));
            }
        }

        return costs;
    }

    public static void markReady(ServerPlayer player, Set<String> insuredSlots, boolean cancelled) {
        UUID playerUUID = player.getUUID();
        UUID sessionKey = playerToSession.get(playerUUID);
        if (sessionKey == null) return;

        InsuranceSession session = pendingSessions.get(sessionKey);
        if (session == null) return;

        if (cancelled) {
            cancelSessionAndNotify(sessionKey, player.level().getServer().overworld());
            return;
        }

        session.selectedSlots.put(playerUUID, insuredSlots);
        session.readyStatus.put(playerUUID, true);

        ServerLevel overworld = player.level().getServer().overworld();

        // Broadcast updated status to all session players
        broadcastStatus(session, overworld, false, false);

        // Check if all ready
        if (isAllReady(session)) {
            onAllReady(session, overworld);
        }
    }

    private static boolean isAllReady(InsuranceSession session) {
        // Key holder must be ready
        if (!session.readyStatus.getOrDefault(session.keyHolderUUID, false)) {
            return false;
        }
        // All party members must be ready
        for (UUID memberId : session.partyMembers) {
            if (!session.readyStatus.getOrDefault(memberId, false)) {
                return false;
            }
        }
        return true;
    }

    private static void onAllReady(InsuranceSession session, ServerLevel overworld) {
        ServerPlayer keyUser = overworld.getServer().getPlayerList().getPlayer(session.keyHolderUUID);
        if (keyUser == null) {
            cancelSessionAndNotify(session.keyHolderUUID, overworld);
            return;
        }

        EconomyManager eco = EconomyManager.get(overworld);

        // Collect all players and verify wallet balances
        List<ServerPlayer> allPlayers = new ArrayList<>();
        allPlayers.add(keyUser);
        for (UUID memberId : session.partyMembers) {
            if (memberId.equals(session.keyHolderUUID)) continue;
            ServerPlayer member = overworld.getServer().getPlayerList().getPlayer(memberId);
            if (member != null) {
                allPlayers.add(member);
            }
        }

        // Deduct insurance costs from each player's wallet
        for (ServerPlayer p : allPlayers) {
            Set<String> slots = session.selectedSlots.getOrDefault(p.getUUID(), Set.of());
            if (slots.isEmpty()) continue;

            // Recalculate total cost server-side (don't trust client)
            int totalCost = calculateTotalCost(p, slots, session.tier, overworld);
            if (!eco.spendWallet(p.getUUID(), totalCost)) {
                // Not enough coins — cancel session
                p.sendSystemMessage(Component.literal("Not enough MegaCoins in wallet! Need " + totalCost).withStyle(ChatFormatting.RED));
                cancelSessionAndNotify(session.keyHolderUUID, overworld);
                return;
            }
            p.sendSystemMessage(Component.literal("Insurance paid: " + totalCost + " MegaCoins").withStyle(ChatFormatting.GOLD));
        }

        // Notify all — closing screens
        broadcastStatus(session, overworld, true, false);

        // Execute dungeon entry (the original DungeonKeyItem logic)
        executeDungeonEntry(keyUser, session.tier, session.hand, session.partyMembers, session, overworld);

        // Cleanup pending session
        cleanupSession(session.keyHolderUUID);
    }

    private static int calculateTotalCost(ServerPlayer player, Set<String> slots, DungeonTier tier, ServerLevel overworld) {
        int total = 0;
        for (String slot : slots) {
            ItemStack stack = getItemForSlot(player, slot, overworld);
            if (!stack.isEmpty()) {
                total += InsuranceCosts.getCost(stack, tier);
            }
        }
        return total;
    }

    public static ItemStack getItemForSlot(ServerPlayer player, String slotName, ServerLevel overworld) {
        if (slotName.startsWith("armor_")) {
            String equipName = slotName.substring(6); // e.g. "head", "chest", "legs", "feet"
            EquipmentSlot equipSlot = EquipmentSlot.byName(equipName);
            return player.getItemBySlot(equipSlot);
        } else if (slotName.equals("mainhand")) {
            return player.getMainHandItem();
        } else if (slotName.equals("offhand")) {
            return player.getOffhandItem();
        } else if (slotName.startsWith("accessory_")) {
            String accName = slotName.substring(10); // e.g. "BACK", "BELT", etc.
            try {
                AccessorySlotType slotType = AccessorySlotType.valueOf(accName);
                return LibAccessoryLookup.getEquipped(player, slotType);
            } catch (IllegalArgumentException e) {
                return ItemStack.EMPTY;
            }
        }
        return ItemStack.EMPTY;
    }

    private static void executeDungeonEntry(ServerPlayer keyUser, DungeonTier tier, InteractionHand hand,
                                            Set<UUID> partyMembers, InsuranceSession session, ServerLevel overworld) {
        // Consume key
        ItemStack stack = keyUser.getItemInHand(hand);
        stack.shrink(1);
        keyUser.level().playSound(null, keyUser.blockPosition(), SoundEvents.END_PORTAL_SPAWN, SoundSource.PLAYERS, 0.8f, 1.2f);

        // Create dungeon
        DungeonManager manager = DungeonManager.get(overworld);
        String instanceId = manager.createDungeon(keyUser, tier);
        if (instanceId == null) {
            keyUser.getItemInHand(hand).grow(1);
            keyUser.sendSystemMessage(Component.literal("Failed to create dungeon!").withStyle(ChatFormatting.RED));
            return;
        }

        // Store active insurance for this dungeon instance
        Map<UUID, Set<String>> instanceInsurance = new HashMap<>();
        instanceInsurance.put(keyUser.getUUID(), session.selectedSlots.getOrDefault(keyUser.getUUID(), Set.of()));
        for (UUID memberId : partyMembers) {
            instanceInsurance.put(memberId, session.selectedSlots.getOrDefault(memberId, Set.of()));
        }
        activeInsurance.put(instanceId, instanceInsurance);

        // Teleport party members
        if (!partyMembers.isEmpty()) {
            DungeonManager.DungeonInstance instance = manager.getInstance(instanceId);
            if (instance != null) {
                ServerLevel dungeonLevel = overworld.getServer().getLevel(MegaModDimensions.DUNGEON);
                if (dungeonLevel != null) {
                    BlockPos entrance = instance.entrancePos != null ? instance.entrancePos : instance.blockPos.offset(5, 1, 5);
                    // Safety: ensure entrance isn't inside a solid block
                    entrance = ensureNotInsideBlock(dungeonLevel, entrance);
                    for (UUID memberId : partyMembers) {
                        if (memberId.equals(keyUser.getUUID())) continue;
                        ServerPlayer member = overworld.getServer().getPlayerList().getPlayer(memberId);
                        if (member != null) {
                            DimensionHelper.teleportToDimension(member, MegaModDimensions.DUNGEON, entrance, 0.0f, 0.0f);
                            manager.addPlayerToDungeon(memberId, instanceId);
                            manager.syncToPlayer(member, instance);
                            member.sendSystemMessage(Component.literal("Your party entered a " + tier.getDisplayName() + " dungeon!").withStyle(ChatFormatting.LIGHT_PURPLE));
                            member.level().playSound(null, member.blockPosition(), SoundEvents.END_PORTAL_SPAWN, SoundSource.PLAYERS, 0.8f, 1.2f);
                        }
                    }
                }
            }
        }
    }

    private static void broadcastStatus(InsuranceSession session, ServerLevel overworld, boolean allReady, boolean cancelled) {
        JsonObject readyMap = new JsonObject();

        // Key holder
        ServerPlayer keyPlayer = overworld.getServer().getPlayerList().getPlayer(session.keyHolderUUID);
        if (keyPlayer != null) {
            readyMap.addProperty(keyPlayer.getGameProfile().name(),
                    session.readyStatus.getOrDefault(session.keyHolderUUID, false));
        }
        // Party members
        for (UUID memberId : session.partyMembers) {
            ServerPlayer member = overworld.getServer().getPlayerList().getPlayer(memberId);
            if (member != null) {
                readyMap.addProperty(member.getGameProfile().name(),
                        session.readyStatus.getOrDefault(memberId, false));
            }
        }

        InsuranceStatusPayload payload = new InsuranceStatusPayload(readyMap.toString(), allReady, cancelled);

        // Send to all
        if (keyPlayer != null) {
            PacketDistributor.sendToPlayer(keyPlayer, payload, new CustomPacketPayload[0]);
        }
        for (UUID memberId : session.partyMembers) {
            if (memberId.equals(session.keyHolderUUID)) continue;
            ServerPlayer member = overworld.getServer().getPlayerList().getPlayer(memberId);
            if (member != null) {
                PacketDistributor.sendToPlayer(member, payload, new CustomPacketPayload[0]);
            }
        }
    }

    public static void cancelSession(UUID keyHolderUUID) {
        InsuranceSession session = pendingSessions.remove(keyHolderUUID);
        if (session != null) {
            cleanupSession(keyHolderUUID);
        }
    }

    public static void cancelSessionAndNotify(UUID keyHolderUUID, ServerLevel overworld) {
        InsuranceSession session = pendingSessions.get(keyHolderUUID);
        if (session != null) {
            broadcastStatus(session, overworld, false, true);

            // Send cancel message to all players
            ServerPlayer keyPlayer = overworld.getServer().getPlayerList().getPlayer(keyHolderUUID);
            if (keyPlayer != null) {
                keyPlayer.sendSystemMessage(Component.literal("Dungeon entry cancelled.").withStyle(ChatFormatting.YELLOW));
            }
            for (UUID memberId : session.partyMembers) {
                if (memberId.equals(keyHolderUUID)) continue;
                ServerPlayer member = overworld.getServer().getPlayerList().getPlayer(memberId);
                if (member != null) {
                    member.sendSystemMessage(Component.literal("Dungeon entry cancelled.").withStyle(ChatFormatting.YELLOW));
                }
            }
        }
        cleanupSession(keyHolderUUID);
    }

    private static void cleanupSession(UUID keyHolderUUID) {
        InsuranceSession session = pendingSessions.remove(keyHolderUUID);
        if (session != null) {
            playerToSession.remove(keyHolderUUID);
            for (UUID memberId : session.partyMembers) {
                playerToSession.remove(memberId);
            }
        }
    }

    /**
     * Called on server tick to check for expired sessions.
     */
    public static void tickSessions(ServerLevel overworld) {
        long now = System.currentTimeMillis();
        List<UUID> expired = new ArrayList<>();
        for (Map.Entry<UUID, InsuranceSession> entry : pendingSessions.entrySet()) {
            if (now - entry.getValue().createdAt > SESSION_TIMEOUT_MS) {
                expired.add(entry.getKey());
            }
        }
        for (UUID keyHolder : expired) {
            MegaMod.LOGGER.info("Insurance session timed out for {}", keyHolder);
            cancelSessionAndNotify(keyHolder, overworld);
        }
    }

    /**
     * Called when a player logs out — cancel any session they're in.
     */
    public static void onPlayerLogout(UUID playerUUID, ServerLevel overworld) {
        UUID sessionKey = playerToSession.get(playerUUID);
        if (sessionKey != null) {
            cancelSessionAndNotify(sessionKey, overworld);
        }
    }

    /**
     * Get insured slots for a player in a dungeon instance.
     * Returns null if no insurance exists.
     */
    public static Set<String> getInsuredSlots(String instanceId, UUID playerUUID) {
        Map<UUID, Set<String>> instanceMap = activeInsurance.get(instanceId);
        if (instanceMap == null) return null;
        return instanceMap.get(playerUUID);
    }

    /**
     * Clean up active insurance when a dungeon is cleaned up.
     */
    public static void clearDungeonInsurance(String instanceId) {
        activeInsurance.remove(instanceId);
    }

    /**
     * Check if a player has a pending insurance session.
     */
    public static boolean hasPendingSession(UUID playerUUID) {
        return playerToSession.containsKey(playerUUID);
    }

    public static class InsuranceSession {
        public final DungeonTier tier;
        public final UUID keyHolderUUID;
        public final InteractionHand hand;
        public final Set<UUID> partyMembers;
        public final Map<UUID, Set<String>> selectedSlots = new HashMap<>();
        public final Map<UUID, Boolean> readyStatus = new HashMap<>();
        public final long createdAt;

        public InsuranceSession(DungeonTier tier, UUID keyHolderUUID, InteractionHand hand,
                                Set<UUID> partyMembers, long createdAt) {
            this.tier = tier;
            this.keyHolderUUID = keyHolderUUID;
            this.hand = hand;
            this.partyMembers = partyMembers;
            this.createdAt = createdAt;
        }
    }

    /**
     * Ensures the spawn position isn't inside a solid block.
     * Scans upward up to 10 blocks to find 2 vertical air blocks; if none found,
     * clears a small pocket at the original position.
     */
    private static BlockPos ensureNotInsideBlock(ServerLevel level, BlockPos pos) {
        if (level.getBlockState(pos).isAir() && level.getBlockState(pos.above()).isAir()) {
            return pos;
        }
        for (int dy = 1; dy <= 10; dy++) {
            BlockPos check = pos.above(dy);
            if (level.getBlockState(check).isAir() && level.getBlockState(check.above()).isAir()) {
                return check;
            }
        }
        MegaMod.LOGGER.warn("InsuranceManager: Party spawn {} is inside blocks, clearing pocket", pos);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                for (int dy = 0; dy <= 2; dy++) {
                    level.setBlockAndUpdate(pos.offset(dx, dy, dz),
                            net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
                }
            }
        }
        return pos;
    }
}
