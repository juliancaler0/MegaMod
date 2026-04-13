package com.ultra.megamod.lib.accessories.utils;

import com.ultra.megamod.mixin.accessories.ItemStackAccessor;
import com.ultra.megamod.lib.accessories.pond.stack.PatchedDataComponentMapExtension;
import com.ultra.megamod.lib.accessories.owo.util.EventStream;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public interface ItemStackMutation {

    static EventStream<ItemStackMutation> getEvent(ItemStack stack) {
        return ((PatchedDataComponentMapExtension) (Object) ((ItemStackAccessor) (Object) stack).accessories$components()).accessories$getMutationEvent(stack);
    }

    void onMutation(ItemStack stack, List<DataComponentType<?>> types);
}
