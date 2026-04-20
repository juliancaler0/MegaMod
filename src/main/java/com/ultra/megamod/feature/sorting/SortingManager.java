package com.ultra.megamod.feature.sorting;

import com.ultra.megamod.feature.toggles.FeatureToggleManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.*;

public class SortingManager {

    /**
     * Sort the container slots of the player's currently open menu.
     * Only sorts non-player-inventory slots (i.e., the chest/container portion).
     */
    public static void sortContainer(ServerPlayer player, SortAlgorithm algorithm) {
        ServerLevel level = (ServerLevel) player.level();
        if (!FeatureToggleManager.get(level).isEnabled("sorting_system")) return;

        AbstractContainerMenu menu = player.containerMenu;
        if (menu == null || menu == player.inventoryMenu) {
            // Sort player inventory (slots 9-35, skip hotbar 0-8)
            sortSlotRange(player, player.inventoryMenu, 9, 36, algorithm);
            return;
        }

        // Find the container slots (not player inventory)
        // Container slots are typically the first N slots before player inventory starts
        int containerSlotCount = menu.slots.size() - 36; // 36 = player inventory size
        if (containerSlotCount <= 0) return;

        sortSlotRange(player, menu, 0, containerSlotCount, algorithm);
    }

    private static void sortSlotRange(ServerPlayer player, AbstractContainerMenu menu, int startSlot, int endSlot, SortAlgorithm algorithm) {
        // Skip ranges containing output-only / non-accepting slots (crafting result,
        // armor slots with validators, etc). Use a probe stack so we detect empty
        // output slots too — checking only the existing item missed crafting result
        // slots when they were empty, and sorting would then write items into them
        // which the crafting system clears on the next tick, effectively deleting items.
        ItemStack probe = new ItemStack(Items.STONE);
        for (int i = startSlot; i < endSlot && i < menu.slots.size(); i++) {
            Slot slot = menu.slots.get(i);
            ItemStack existing = slot.getItem();
            if (!existing.isEmpty() && !slot.mayPlace(existing)) return;
            if (!slot.mayPlace(probe)) return;
        }

        // Collect all items from the range
        List<ItemStack> items = new ArrayList<>();
        for (int i = startSlot; i < endSlot && i < menu.slots.size(); i++) {
            Slot slot = menu.slots.get(i);
            ItemStack stack = slot.getItem();
            if (!stack.isEmpty()) {
                items.add(stack.copy());
            }
        }

        if (items.isEmpty()) return;

        // Merge partial stacks
        items = mergeStacks(items);

        // Sort
        items.sort(algorithm.getComparator());

        // Write back
        int itemIdx = 0;
        for (int i = startSlot; i < endSlot && i < menu.slots.size(); i++) {
            Slot slot = menu.slots.get(i);
            if (itemIdx < items.size()) {
                slot.set(items.get(itemIdx));
                itemIdx++;
            } else {
                slot.set(ItemStack.EMPTY);
            }
        }

        menu.broadcastChanges();
    }

    private static List<ItemStack> mergeStacks(List<ItemStack> items) {
        List<ItemStack> merged = new ArrayList<>();
        for (ItemStack stack : items) {
            boolean found = false;
            for (ItemStack existing : merged) {
                if (ItemStack.isSameItemSameComponents(existing, stack)
                    && existing.getCount() < existing.getMaxStackSize()) {
                    int canAdd = existing.getMaxStackSize() - existing.getCount();
                    int toAdd = Math.min(canAdd, stack.getCount());
                    existing.grow(toAdd);
                    stack.shrink(toAdd);
                    if (stack.isEmpty()) {
                        found = true;
                        break;
                    }
                }
            }
            if (!found && !stack.isEmpty()) {
                merged.add(stack.copy());
            }
        }
        return merged;
    }
}
