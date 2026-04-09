package com.ultra.megamod.feature.backpacks;

import net.minecraft.world.SimpleContainer;

/**
 * Manages the backpack's inventory storage.
 * Wraps a SimpleContainer sized to the backpack's tier.
 */
public class BackpackInventory extends SimpleContainer {
    private final BackpackTier tier;

    public BackpackInventory(BackpackTier tier) {
        super(tier.getStorageSlots());
        this.tier = tier;
    }

    public BackpackTier getTier() { return tier; }
}
