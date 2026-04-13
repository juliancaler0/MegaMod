package com.ultra.megamod.lib.accessories.utils;

import com.ultra.megamod.lib.accessories.pond.stack.ItemStackExtension;
import com.ultra.megamod.lib.accessories.owo.util.EventStream;
import net.minecraft.world.item.ItemStack;

public interface ItemStackResize {
    static EventStream<ItemStackResize> getEvent(ItemStack stack) {
        return ((ItemStackExtension) (Object) stack).accessories$getResizeEvent();
    }

    void onResize(ItemStack stack, int prevSize);
}
