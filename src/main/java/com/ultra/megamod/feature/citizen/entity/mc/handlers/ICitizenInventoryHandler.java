package com.ultra.megamod.feature.citizen.entity.mc.handlers;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/**
 * Handler interface for all inventory-related citizen entity methods.
 * Ported from MineColonies' ICitizenInventoryHandler.
 */
public interface ICitizenInventoryHandler {

    /**
     * Returns the first slot in the inventory with a specific item.
     */
    int findFirstSlotInInventoryWith(Item targetItem);

    /**
     * Returns the first slot in the inventory with a specific block.
     */
    int findFirstSlotInInventoryWith(Block block);

    /**
     * Returns the amount of a certain block in the inventory.
     */
    int getItemCountInInventory(Block block);

    /**
     * Returns the amount of a certain item in the inventory.
     */
    int getItemCountInInventory(Item targetItem);

    /**
     * Checks if citizen has a certain block in the inventory.
     */
    boolean hasItemInInventory(Block block);

    /**
     * Checks if citizen has a certain item in the inventory.
     */
    boolean hasItemInInventory(Item item);

    /**
     * Checks whether or not the inventory is full.
     */
    boolean isInventoryFull();
}
