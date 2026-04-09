package com.ultra.megamod.feature.citizen.building.module;

import net.minecraft.world.item.ItemStack;

import java.util.Map;

/**
 * Module interface for buildings that track minimum inventory thresholds.
 * When stock falls below the minimum, the building can request resupply
 * through the colony's request system.
 */
public interface IMinimumStockModule extends IBuildingModule {

    /**
     * Returns the map of items to their minimum required counts.
     *
     * @return unmodifiable map of item stacks to minimum counts
     */
    Map<ItemStack, Integer> getMinimumStock();

    /**
     * Sets the minimum stock threshold for an item.
     *
     * @param item  the item to track
     * @param count the minimum count required
     */
    void setMinimumStock(ItemStack item, int count);

    /**
     * Removes an item from minimum stock tracking.
     *
     * @param item the item to stop tracking
     */
    void removeMinimumStock(ItemStack item);
}
