package com.ultra.megamod.mixin.rangedweapon.item;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.ultra.megamod.lib.rangedweapon.api.EntityAttributes_RangedWeapon;
import com.ultra.megamod.lib.rangedweapon.api.RangedConfig;
import com.ultra.megamod.lib.rangedweapon.internal.RangedItemSettings;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(BowItem.class)
public class BowItemMixin {
    public float getPullProgress_RWA(int useTicks, LivingEntity user) {
        var pullTime = user.getAttributeValue(EntityAttributes_RangedWeapon.PULL_TIME.entry);
        var pullTimeTicks = Math.round(pullTime * 20);
//        System.out.println("Bow Pull time: " + pullTimeTicks);
        float f = (float)useTicks / pullTimeTicks;
        f = (f * f + f * 2.0F) / 3.0F;
        if (f > 1.0F) {
            f = 1.0F;
        }
        return f;
    }

    @ModifyVariable(method = "<init>", at = @At("HEAD"), ordinal = 0)
    private static Item.Properties applyDefaultAttributes(Item.Properties settings) {
        if (((RangedItemSettings) settings).getRangedAttributes() == null) {
            return ((RangedItemSettings) settings).rangedAttributes(RangedConfig.BOW);
        } else {
            return settings;
        }
    }

    /**
     * Apply custom pull time
     */
    @WrapOperation(
            method = "releaseUsing",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/BowItem;getPowerForTime(I)F")
    )
    private float applyCustomPullTime(
            // Mixin parameters
            int ticks, Operation<Float> original,
            // Context parameters
            ItemStack stack, Level world, LivingEntity user, int remainingUseTicks) {
        return getPullProgress_RWA(ticks, user);
    }
}
