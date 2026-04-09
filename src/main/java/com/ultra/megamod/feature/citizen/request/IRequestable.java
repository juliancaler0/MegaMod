package com.ultra.megamod.feature.citizen.request;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/**
 * Describes what is being requested in the colony request system.
 * Implementations define the matching logic for whether an item stack satisfies the request.
 */
public interface IRequestable {

    /**
     * Checks if the given item stack satisfies this request (item type AND count).
     *
     * @param stack the item stack to test
     * @return true if the stack matches the request
     */
    boolean matches(ItemStack stack);

    /**
     * Checks if the given item stack is the right item type for this request,
     * regardless of count. Used during warehouse scanning and pickup.
     *
     * @param stack the item stack to test
     * @return true if the item type matches
     */
    default boolean matchesItem(ItemStack stack) {
        return matches(stack);
    }

    /**
     * Returns a human-readable description of this request.
     *
     * @return the description string
     */
    String getDescription();

    /**
     * Returns the count of items needed.
     *
     * @return the requested count
     */
    int getCount();

    /**
     * Serializes this requestable to NBT.
     *
     * @return the compound tag
     */
    CompoundTag toNbt();
}
