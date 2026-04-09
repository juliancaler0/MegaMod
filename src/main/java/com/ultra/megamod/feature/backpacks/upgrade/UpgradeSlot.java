package com.ultra.megamod.feature.backpacks.upgrade;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Slot that only accepts UpgradeItem instances.
 * Used for the upgrade slots in the backpack menu.
 */
public class UpgradeSlot extends Slot {

    public UpgradeSlot(Container container, int index, int x, int y) {
        super(container, index, x, y);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return stack.getItem() instanceof UpgradeItem;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }
}
