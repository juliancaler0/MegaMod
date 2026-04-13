package com.ultra.megamod.lib.combatroll.enchantments;

import net.minecraft.world.item.ItemStack;

public interface CustomConditionalEnchantment {
    interface Condition {
        boolean isAcceptableItem(ItemStack stack);
    }
    void setCondition(Condition condition);
}
