package com.ultra.megamod.feature.backpacks.menu;

import com.ultra.megamod.feature.backpacks.BackpackItem;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class BackpackSlot extends Slot {

    public BackpackSlot(Container container, int index, int x, int y) {
        super(container, index, x, y);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return !(stack.getItem() instanceof BackpackItem);
    }
}
