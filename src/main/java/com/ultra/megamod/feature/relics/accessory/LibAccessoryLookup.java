package com.ultra.megamod.feature.relics.accessory;

import com.ultra.megamod.feature.relics.RelicItem;
import com.ultra.megamod.feature.relics.data.AccessorySlotType;
import com.ultra.megamod.lib.accessories.api.AccessoriesCapability;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Bridge that translates our legacy {@link AccessorySlotType} vocabulary into the
 * lib/accessories slot names and queries the lib's {@link AccessoriesCapability}
 * for equipped relic stacks.
 *
 * <p>This lets the existing ability-casting / passive-tick code keep working while
 * storage actually lives in the lib capability. Once every consumer is migrated,
 * the legacy {@link AccessoryManager} can be deleted.</p>
 */
public final class LibAccessoryLookup {
    private LibAccessoryLookup() {}

    /**
     * Returns the first matching item in the lib slot that corresponds to the given
     * legacy slot type — e.g. {@link AccessorySlotType#RING_LEFT} and {@code RING_RIGHT}
     * both resolve against the lib's {@code ring} container (index 0 vs 1).
     * Returns {@link ItemStack#EMPTY} if nothing equipped.
     */
    public static ItemStack getEquipped(ServerPlayer player, AccessorySlotType slot) {
        if (slot == null || slot == AccessorySlotType.NONE) return ItemStack.EMPTY;
        AccessoriesCapability cap = AccessoriesCapability.get(player);
        if (cap == null) return ItemStack.EMPTY;

        String libSlotName = toLibSlotName(slot);
        if (libSlotName == null) return ItemStack.EMPTY;
        var container = cap.getContainers().get(libSlotName);
        if (container == null) return ItemStack.EMPTY;

        int index = legacyLeftRightIndex(slot);
        var accessories = container.getAccessories();
        if (accessories == null || accessories.getContainerSize() <= index) return ItemStack.EMPTY;
        ItemStack stack = accessories.getItem(index);
        return stack != null ? stack : ItemStack.EMPTY;
    }

    /**
     * Returns all equipped relic stacks across every slot, for tick/event handlers
     * that need to iterate every active relic the player has on.
     */
    public static List<ItemStack> getAllEquippedRelics(ServerPlayer player) {
        AccessoriesCapability cap = AccessoriesCapability.get(player);
        if (cap == null) return List.of();
        List<ItemStack> out = new ArrayList<>();
        for (var container : cap.getContainers().values()) {
            var accessories = container.getAccessories();
            if (accessories == null) continue;
            for (int i = 0; i < accessories.getContainerSize(); i++) {
                ItemStack s = accessories.getItem(i);
                if (s != null && !s.isEmpty() && s.getItem() instanceof RelicItem) out.add(s);
            }
        }
        return out;
    }

    /**
     * Legacy {@code RING_LEFT}/{@code RING_RIGHT} and {@code HANDS_LEFT}/{@code HANDS_RIGHT}
     * map to indices 0/1 of the same lib container.
     */
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
