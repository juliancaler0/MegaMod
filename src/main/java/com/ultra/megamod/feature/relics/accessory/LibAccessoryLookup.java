package com.ultra.megamod.feature.relics.accessory;

import com.ultra.megamod.feature.relics.RelicItem;
import com.ultra.megamod.feature.relics.data.AccessorySlotType;
import com.ultra.megamod.feature.relics.network.AccessoryPayload;
import com.ultra.megamod.lib.accessories.api.AccessoriesCapability;
import com.ultra.megamod.lib.accessories.api.AccessoriesContainer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.EnumMap;
import java.util.Map;

/**
 * Bridge that translates the legacy {@link AccessorySlotType} vocabulary into
 * lib/accessories slot names and routes all reads/writes through
 * {@link AccessoriesCapability}. This is now the single source of truth for
 * equipped-relic state — there is no separate legacy save file.
 */
public final class LibAccessoryLookup {
    private LibAccessoryLookup() {}

    public static ItemStack getEquipped(ServerPlayer player, AccessorySlotType slot) {
        if (slot == null || slot == AccessorySlotType.NONE) return ItemStack.EMPTY;
        AccessoriesContainer container = containerFor(player, slot);
        if (container == null) return ItemStack.EMPTY;
        int index = legacyLeftRightIndex(slot);
        var accessories = container.getAccessories();
        if (accessories == null || accessories.getContainerSize() <= index) return ItemStack.EMPTY;
        ItemStack stack = accessories.getItem(index);
        return stack != null ? stack : ItemStack.EMPTY;
    }

    public static Map<AccessorySlotType, ItemStack> getAllEquipped(ServerPlayer player) {
        EnumMap<AccessorySlotType, ItemStack> out = new EnumMap<>(AccessorySlotType.class);
        AccessoriesCapability cap = AccessoriesCapability.get(player);
        if (cap == null) return out;
        for (AccessorySlotType slot : AccessorySlotType.values()) {
            if (slot == AccessorySlotType.NONE) continue;
            ItemStack stack = getEquipped(player, slot);
            if (!stack.isEmpty()) out.put(slot, stack);
        }
        return out;
    }

    public static java.util.List<ItemStack> getAllEquippedRelics(ServerPlayer player) {
        AccessoriesCapability cap = AccessoriesCapability.get(player);
        if (cap == null) return java.util.List.of();
        java.util.List<ItemStack> list = new java.util.ArrayList<>();
        for (AccessoriesContainer container : cap.getContainers().values()) {
            var accessories = container.getAccessories();
            if (accessories == null) continue;
            for (int i = 0; i < accessories.getContainerSize(); i++) {
                ItemStack s = accessories.getItem(i);
                if (s != null && !s.isEmpty() && s.getItem() instanceof RelicItem) list.add(s);
            }
        }
        return list;
    }

    /**
     * Writes the given stack into the lib container slot matching the legacy type. The
     * stack is copied defensively so callers can continue mutating the argument.
     * Returns {@code true} if the write succeeded (slot container exists and index is valid).
     */
    public static boolean setEquipped(ServerPlayer player, AccessorySlotType slot, ItemStack stack) {
        if (slot == null || slot == AccessorySlotType.NONE) return false;
        AccessoriesContainer container = containerFor(player, slot);
        if (container == null) return false;
        int index = legacyLeftRightIndex(slot);
        var accessories = container.getAccessories();
        if (accessories == null || accessories.getContainerSize() <= index) return false;
        accessories.setItem(index, stack.isEmpty() ? ItemStack.EMPTY : stack.copy());
        return true;
    }

    /**
     * Removes any stack in the legacy slot's lib index and returns what was removed,
     * or {@link ItemStack#EMPTY} if nothing was there.
     */
    public static ItemStack removeEquipped(ServerPlayer player, AccessorySlotType slot) {
        if (slot == null || slot == AccessorySlotType.NONE) return ItemStack.EMPTY;
        AccessoriesContainer container = containerFor(player, slot);
        if (container == null) return ItemStack.EMPTY;
        int index = legacyLeftRightIndex(slot);
        var accessories = container.getAccessories();
        if (accessories == null || accessories.getContainerSize() <= index) return ItemStack.EMPTY;
        ItemStack previous = accessories.getItem(index);
        if (previous == null || previous.isEmpty()) return ItemStack.EMPTY;
        ItemStack removed = previous.copy();
        accessories.setItem(index, ItemStack.EMPTY);
        return removed;
    }

    /**
     * Pushes the legacy-compat {@link AccessoryPayload.AccessorySyncPayload} to the player.
     * Client-side MegaMod renderers (ability bars, inventory overlay, insurance screen)
     * key off this payload's static {@code clientEquipped} map — the lib has its own
     * sync for the unified GUI, but our custom HUD predates the lib port.
     */
    public static void syncToClient(ServerPlayer player) {
        CompoundTag tag = new CompoundTag();
        for (Map.Entry<AccessorySlotType, ItemStack> entry : getAllEquipped(player).entrySet()) {
            ItemStack stack = entry.getValue();
            if (stack.isEmpty()) continue;
            String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
            tag.putString(entry.getKey().name(), itemId);
        }
        PacketDistributor.sendToPlayer(player, new AccessoryPayload.AccessorySyncPayload(tag));
    }

    private static AccessoriesContainer containerFor(ServerPlayer player, AccessorySlotType slot) {
        AccessoriesCapability cap = AccessoriesCapability.get(player);
        if (cap == null) return null;
        String libSlotName = toLibSlotName(slot);
        if (libSlotName == null) return null;
        return cap.getContainers().get(libSlotName);
    }

    private static int legacyLeftRightIndex(AccessorySlotType slot) {
        return switch (slot) {
            case RING_RIGHT, HANDS_RIGHT -> 1;
            default -> 0;
        };
    }

    private static String toLibSlotName(AccessorySlotType slot) {
        return switch (slot) {
            case BACK -> "back";
            case BELT -> "belt";
            case HANDS_LEFT, HANDS_RIGHT -> "hand";
            case FEET -> "shoes";
            case NECKLACE -> "necklace";
            case RING_LEFT, RING_RIGHT -> "ring";
            case HEAD -> "hat";
            case FACE -> "face";
            case NONE -> null;
        };
    }
}
