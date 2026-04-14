/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.NbtAccounter
 *  net.minecraft.nbt.NbtIo
 *  net.minecraft.nbt.Tag
 *  net.minecraft.resources.Identifier
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.component.CustomData
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.storage.LevelResource
 */
package com.ultra.megamod.feature.relics.accessory;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.relics.data.AccessorySlotType;
import java.io.File;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.LevelResource;
import com.ultra.megamod.feature.relics.accessory.LibAccessoryLookup;

public class AccessoryManager {
    private static AccessoryManager INSTANCE;
    private static final String FILE_NAME = "megamod_accessories.dat";
    private final Map<UUID, Map<AccessorySlotType, ItemStack>> playerAccessories = new HashMap<UUID, Map<AccessorySlotType, ItemStack>>();
    private boolean dirty = false;

    public static AccessoryManager get(ServerLevel level) {
        if (INSTANCE == null) {
            INSTANCE = new AccessoryManager();
            INSTANCE.loadFromDisk(level);
        }
        return INSTANCE;
    }

    public static void reset() {
        INSTANCE = null;
    }

    private void markDirty() {
        this.dirty = true;
    }

    private Map<AccessorySlotType, ItemStack> getOrCreate(UUID playerId) {
        return this.playerAccessories.computeIfAbsent(playerId, k -> new EnumMap<>(AccessorySlotType.class));
    }

    public ItemStack getEquipped(UUID playerId, AccessorySlotType slot) {
        // Phase 2 bridge: route all reads through the lib/accessories capability so
        // relics equipped via the unified accessories GUI are visible to the ability system.
        if (slot != AccessorySlotType.NONE) {
            ItemStack libStack = bridgeLookup(playerId, slot);
            if (!libStack.isEmpty()) return libStack;
        }
        // Fallback for legacy save data + for NONE slot (weapons held in hand)
        Map<AccessorySlotType, ItemStack> slots = this.playerAccessories.get(playerId);
        if (slots == null) {
            return ItemStack.EMPTY;
        }
        return slots.getOrDefault(slot, ItemStack.EMPTY);
    }

    /** Resolves the player by UUID from the active server and asks the lib capability. */
    private ItemStack bridgeLookup(UUID playerId, AccessorySlotType slot) {
        try {
            var server = net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer();
            if (server == null) return ItemStack.EMPTY;
            var player = server.getPlayerList().getPlayer(playerId);
            if (player == null) return ItemStack.EMPTY;
            return LibAccessoryLookup.getEquipped(player, slot);
        } catch (Throwable t) {
            return ItemStack.EMPTY;
        }
    }

    public void setEquipped(UUID playerId, AccessorySlotType slot, ItemStack stack) {
        Map<AccessorySlotType, ItemStack> slots = this.getOrCreate(playerId);
        if (stack.isEmpty()) {
            slots.remove(slot);
        } else {
            slots.put(slot, stack.copy());
        }
        this.markDirty();
    }

    public ItemStack removeEquipped(UUID playerId, AccessorySlotType slot) {
        Map<AccessorySlotType, ItemStack> slots = this.playerAccessories.get(playerId);
        if (slots == null) {
            return ItemStack.EMPTY;
        }
        ItemStack previous = slots.remove(slot);
        if (previous != null) {
            this.markDirty();
            return previous;
        }
        return ItemStack.EMPTY;
    }

    public Map<AccessorySlotType, ItemStack> getAllEquipped(UUID playerId) {
        // Phase 2 bridge: merge lib-equipped relics with any legacy entries so nothing is missed
        // while save data is in transition.
        EnumMap<AccessorySlotType, ItemStack> merged = new EnumMap<>(AccessorySlotType.class);

        // 1) lib/accessories (authoritative source of truth for newly equipped items)
        try {
            var server = net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer();
            if (server != null) {
                var player = server.getPlayerList().getPlayer(playerId);
                if (player != null) {
                    for (AccessorySlotType slot : AccessorySlotType.values()) {
                        if (slot == AccessorySlotType.NONE) continue;
                        ItemStack stack = LibAccessoryLookup.getEquipped(player, slot);
                        if (!stack.isEmpty()) merged.put(slot, stack);
                    }
                }
            }
        } catch (Throwable ignored) {}

        // 2) legacy map — fills in anything the lib doesn't have (e.g. old saves not yet migrated)
        Map<AccessorySlotType, ItemStack> legacy = this.playerAccessories.get(playerId);
        if (legacy != null) {
            for (var entry : legacy.entrySet()) {
                if (!entry.getValue().isEmpty()) {
                    merged.putIfAbsent(entry.getKey(), entry.getValue());
                }
            }
        }
        return merged;
    }

    public void loadFromDisk(ServerLevel level) {
        try {
            File saveDir = level.getServer().getWorldPath(LevelResource.ROOT).toFile();
            File dataFile = new File(saveDir, "data" + File.separator + FILE_NAME);
            if (!dataFile.exists()) {
                return;
            }
            CompoundTag root = NbtIo.readCompressed(dataFile.toPath(), NbtAccounter.unlimitedHeap());
            CompoundTag players = root.getCompoundOrEmpty("players");
            for (String uuidStr : players.keySet()) {
                UUID uuid = UUID.fromString(uuidStr);
                CompoundTag playerTag = players.getCompoundOrEmpty(uuidStr);
                EnumMap<AccessorySlotType, ItemStack> slots = new EnumMap<AccessorySlotType, ItemStack>(AccessorySlotType.class);
                for (AccessorySlotType slot : AccessorySlotType.values()) {
                    String itemId;
                    CompoundTag slotTag;
                    if (slot == AccessorySlotType.NONE || (slotTag = playerTag.getCompoundOrEmpty(slot.name())).isEmpty() || (itemId = slotTag.getStringOr("item", "")).isEmpty()) continue;
                    Item item = (Item)BuiltInRegistries.ITEM.getValue(Identifier.parse((String)itemId));
                    if (item == null) continue;
                    ItemStack stack = new ItemStack((ItemLike)item);
                    CompoundTag data = slotTag.getCompoundOrEmpty("data");
                    if (!data.isEmpty()) {
                        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
                    }
                    slots.put(slot, stack);
                }
                if (slots.isEmpty()) continue;
                this.playerAccessories.put(uuid, slots);
            }
        }
        catch (Exception e) {
            MegaMod.LOGGER.error("Failed to load accessory data", e);
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
            CompoundTag players = new CompoundTag();
            for (Map.Entry<UUID, Map<AccessorySlotType, ItemStack>> entry : this.playerAccessories.entrySet()) {
                CompoundTag playerTag = new CompoundTag();
                for (Map.Entry<AccessorySlotType, ItemStack> slotEntry : entry.getValue().entrySet()) {
                    ItemStack stack = slotEntry.getValue();
                    if (stack.isEmpty()) continue;
                    CompoundTag slotTag = new CompoundTag();
                    slotTag.putString("item", BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
                    if (stack.has(DataComponents.CUSTOM_DATA)) {
                        slotTag.put("data", stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag());
                    }
                    playerTag.put(slotEntry.getKey().name(), slotTag);
                }
                if (playerTag.isEmpty()) continue;
                players.put(entry.getKey().toString(), playerTag);
            }
            root.put("players", players);
            NbtIo.writeCompressed(root, dataFile.toPath());
            this.dirty = false;
        }
        catch (Exception e) {
            MegaMod.LOGGER.error("Failed to save accessory data", e);
        }
    }
}

