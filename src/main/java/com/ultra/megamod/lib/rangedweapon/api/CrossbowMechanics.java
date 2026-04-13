package com.ultra.megamod.lib.rangedweapon.api;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public class CrossbowMechanics {
    public static class PullTime {
        public static final Provider defaultProvider = (originalPullTime, crossbow, user) -> {
            float f = EnchantmentHelper.modifyCrossbowChargingTime(crossbow, user, 1.25F);
            return Mth.floor(f * 20.0F);
        };
        public static Provider modifier = defaultProvider;
        public interface Provider {
            int getPullTime(int originalPullTime, ItemStack crossbow, LivingEntity user);
        }
    }
}
