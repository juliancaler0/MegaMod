package com.ultra.megamod.feature.backpacks;

import com.ultra.megamod.feature.backpacks.network.BackpackSyncPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server-side singleton tracking which players have a backpack equipped (worn on back).
 * In-memory only -- does not persist across server restarts.
 * Client-side cache is maintained via BackpackSyncPayload.
 */
public class BackpackWearableManager {

    // Server-side: UUID -> ItemStack of equipped backpack
    private static final Map<UUID, ItemStack> equippedBackpacks = new ConcurrentHashMap<>();

    // Client-side cache: UUID -> backpack item registry name (empty string = none)
    private static final Map<UUID, String> clientEquipped = new ConcurrentHashMap<>();

    // ========================
    // Server-side methods
    // ========================

    /**
     * Equip a backpack from the player's inventory.
     * Removes it from the given inventory slot and stores it as worn.
     */
    public static boolean equip(ServerPlayer player, ItemStack backpackStack, int inventorySlot) {
        if (backpackStack.isEmpty() || !(backpackStack.getItem() instanceof BackpackItem)) return false;
        if (isWearing(player.getUUID())) return false; // already wearing one

        // Store a copy
        equippedBackpacks.put(player.getUUID(), backpackStack.copy());

        // Remove from inventory
        player.getInventory().setItem(inventorySlot, ItemStack.EMPTY);

        // Play equip sound
        player.level().playSound(null, player.blockPosition(),
            SoundEvents.ARMOR_EQUIP_LEATHER.value(), SoundSource.PLAYERS, 1.0f, 1.0f);

        // Sync to all clients
        syncToAllClients(player);
        return true;
    }

    /**
     * Equip a backpack that is currently held in the open BackpackMenu.
     * The backpack item is identified by scanning the player's inventory.
     */
    public static boolean equipFromMenu(ServerPlayer player) {
        if (isWearing(player.getUUID())) return false;

        // Find the backpack in the player's inventory
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() instanceof BackpackItem) {
                return equip(player, stack, i);
            }
        }
        return false;
    }

    /**
     * Unequip the worn backpack, returning it to the player's inventory.
     */
    public static boolean unequip(ServerPlayer player) {
        ItemStack removed = equippedBackpacks.remove(player.getUUID());
        if (removed == null || removed.isEmpty()) return false;

        // Add back to inventory
        if (!player.getInventory().add(removed)) {
            // If inventory is full, drop on ground
            player.spawnAtLocation((ServerLevel) player.level(), removed);
        }

        // Play unequip sound
        player.level().playSound(null, player.blockPosition(),
            SoundEvents.ARMOR_EQUIP_LEATHER.value(), SoundSource.PLAYERS, 1.0f, 0.8f);

        // Sync to all clients
        syncToAllClients(player);
        return true;
    }

    /**
     * Clear a player's equipped backpack without returning it (used on disconnect).
     * The backpack is lost -- this matches the in-memory-only design.
     * On disconnect we drop it so the player can pick it back up.
     */
    public static void clearOnDisconnect(ServerPlayer player) {
        ItemStack removed = equippedBackpacks.remove(player.getUUID());
        if (removed != null && !removed.isEmpty()) {
            // Drop on the ground so it's not lost
            player.spawnAtLocation((ServerLevel) player.level(), removed);
        }
    }

    public static boolean isWearing(UUID uuid) {
        ItemStack stack = equippedBackpacks.get(uuid);
        return stack != null && !stack.isEmpty();
    }

    public static ItemStack getEquipped(UUID uuid) {
        ItemStack stack = equippedBackpacks.get(uuid);
        return stack != null ? stack : ItemStack.EMPTY;
    }

    /**
     * Get the registry name of the equipped backpack item, or empty string.
     */
    public static String getEquippedId(UUID uuid) {
        ItemStack stack = equippedBackpacks.get(uuid);
        if (stack == null || stack.isEmpty()) return "";
        return net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
    }

    /**
     * Sync one player's backpack state to all online clients.
     */
    public static void syncToAllClients(ServerPlayer player) {
        String itemId = getEquippedId(player.getUUID());
        BackpackSyncPayload payload = new BackpackSyncPayload(player.getId(), itemId);

        // Send to all players on the server
        for (ServerPlayer target : player.level().getServer().getPlayerList().getPlayers()) {
            PacketDistributor.sendToPlayer(target,
                (CustomPacketPayload) payload,
                (CustomPacketPayload[]) new CustomPacketPayload[0]);
        }
    }

    /**
     * Sync all equipped backpacks to a newly joined player.
     */
    public static void syncAllToPlayer(ServerPlayer newPlayer) {
        for (ServerPlayer online : newPlayer.level().getServer().getPlayerList().getPlayers()) {
            if (isWearing(online.getUUID())) {
                String itemId = getEquippedId(online.getUUID());
                PacketDistributor.sendToPlayer(newPlayer,
                    (CustomPacketPayload) new BackpackSyncPayload(online.getId(), itemId),
                    (CustomPacketPayload[]) new CustomPacketPayload[0]);
            }
        }
    }

    // ========================
    // Client-side cache methods
    // ========================

    /**
     * Called on the client when a sync payload is received.
     */
    public static void setClientEquipped(int entityId, String backpackItemId) {
        if (backpackItemId == null || backpackItemId.isEmpty()) {
            // Remove the entry entirely so isClientWearing returns false immediately
            clientEquippedByEntityId.remove(entityId);
        } else {
            clientEquippedByEntityId.put(entityId, backpackItemId);
        }
    }

    // Client-side: entity ID -> backpack item registry name (empty = none)
    private static final Map<Integer, String> clientEquippedByEntityId = new ConcurrentHashMap<>();

    /**
     * Check if a given entity (by ID) has an equipped backpack on the client.
     */
    public static boolean isClientWearing(int entityId) {
        String id = clientEquippedByEntityId.get(entityId);
        return id != null && !id.isEmpty();
    }

    /**
     * Get the backpack item ID for a given entity on the client.
     */
    public static String getClientBackpackId(int entityId) {
        String id = clientEquippedByEntityId.get(entityId);
        return id != null ? id : "";
    }

    /**
     * Clear client cache for a removed entity.
     */
    public static void clearClientEntry(int entityId) {
        clientEquippedByEntityId.remove(entityId);
    }
}
