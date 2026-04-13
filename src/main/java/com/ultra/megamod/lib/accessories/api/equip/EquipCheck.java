package com.ultra.megamod.lib.accessories.api.equip;

import net.minecraft.world.item.ItemStack;

public interface EquipCheck {
    boolean isValid(ItemStack stack, boolean isSwapping);
}
