package com.ultra.megamod.mixin.rangedweapon.item;

import com.ultra.megamod.lib.rangedweapon.api.CustomRangedWeapon;
import com.ultra.megamod.lib.rangedweapon.api.EntityAttributes_RangedWeapon;
import com.ultra.megamod.lib.rangedweapon.api.RangedConfig;
import com.ultra.megamod.lib.rangedweapon.internal.RangedItemSettings;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CrossbowItem.class)
public class RangedWeaponCrossbowItemMixin {
    @ModifyVariable(method = "<init>", at = @At("HEAD"), ordinal = 0)
    private static Item.Properties applyDefaultAttributes(Item.Properties settings) {
        if (((RangedItemSettings) settings).getRangedAttributes() == null) {
            return ((RangedItemSettings) settings).rangedAttributes(RangedConfig.CROSSBOW);
        } else {
            return settings;
        }
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void postInit(Item.Properties settings, CallbackInfo ci) {
        ((CustomRangedWeapon)this).setTypeBaseline(RangedConfig.CROSSBOW);
    }

    /**
     * Apply custom pull time
     */
    @Inject(method = "getChargeDuration", at = @At("HEAD"), cancellable = true)
    private static void applyCustomPullTime_RWA(ItemStack stack, LivingEntity user, CallbackInfoReturnable<Integer> cir) {
        var item = stack.getItem();
        if (item instanceof CustomRangedWeapon weapon) {
            var pullTime = (float) user.getAttributeValue(EntityAttributes_RangedWeapon.PULL_TIME.entry);
            float f = EnchantmentHelper.modifyCrossbowChargingTime(stack, user, pullTime);
            var pullTimeTicks = Mth.floor(f * 20.0F);
            cir.setReturnValue(pullTimeTicks);
            cir.cancel();
        }
    }
}
