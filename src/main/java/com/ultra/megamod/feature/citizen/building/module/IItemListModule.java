package com.ultra.megamod.feature.citizen.building.module;

import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Module interface for buildings that maintain an item filter list.
 * Used for warehouse filtering, crafting material whitelists,
 * composting input lists, etc.
 */
public interface IItemListModule extends IBuildingModule {

    /**
     * Returns the list of items in this filter.
     *
     * @return unmodifiable list of item stacks
     */
    List<ItemStack> getItemList();

    /**
     * Adds an item to the filter list.
     *
     * @param item the item to add
     * @return true if the item was added, false if already present
     */
    boolean addItem(ItemStack item);

    /**
     * Removes an item from the filter list.
     *
     * @param item the item to remove
     */
    void removeItem(ItemStack item);

    /**
     * Checks whether an item matches any entry on this list.
     *
     * @param item the item to check
     * @return true if the item is on the list
     */
    boolean isOnList(ItemStack item);
}
