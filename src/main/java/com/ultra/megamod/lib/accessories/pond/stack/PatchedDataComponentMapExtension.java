package com.ultra.megamod.lib.accessories.pond.stack;

import com.ultra.megamod.lib.accessories.utils.ItemStackMutation;
import com.ultra.megamod.lib.accessories.owo.util.EventStream;
import net.minecraft.world.item.ItemStack;

public interface PatchedDataComponentMapExtension {
    boolean accessories$hasChanged();

    EventStream<ItemStackMutation> accessories$getMutationEvent(ItemStack stack);
}
