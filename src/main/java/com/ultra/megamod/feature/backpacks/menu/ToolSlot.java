package com.ultra.megamod.feature.backpacks.menu;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Slot that only accepts tools, weapons, and utility items.
 * Used for the quick-access tool slots on the left side of the backpack GUI.
 */
public class ToolSlot extends Slot {

    public ToolSlot(Container container, int index, int x, int y) {
        super(container, index, x, y);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return isToolItem(stack);
    }

    public static boolean isToolItem(ItemStack stack) {
        // Accept anything with durability (tools, weapons, armor, shields, etc.)
        // This covers all swords, pickaxes, axes, shovels, hoes, bows, crossbows,
        // shields, fishing rods, flint & steel, shears, tridents, brushes, spyglasses
        return stack.isDamageableItem() || stack.getMaxStackSize() == 1;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }
}
