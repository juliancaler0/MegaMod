package com.ultra.megamod.lib.accessories.api.events.extra;

import com.ultra.megamod.lib.accessories.api.slot.SlotReference;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public interface OnTotemConsumption {

    OnTotemConsumption DEFAULT_BEHAVIOR = (slotReference, currentStack, damageSource) -> {
        currentStack.shrink(1);

        return currentStack;
    };

    ItemStack onConsumption(SlotReference slotReference, ItemStack currentStack, DamageSource damageSource);
}
