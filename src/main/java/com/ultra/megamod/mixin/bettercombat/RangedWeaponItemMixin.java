package com.ultra.megamod.mixin.bettercombat;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.ultra.megamod.feature.combat.animation.logic.InventoryUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Fixes ranged weapon projectile detection for two-handed wielding.
 * Two-handed weapons disable offhand slot, but ranged weapons should still find offhand ammo.
 * Ported 1:1 from BetterCombat (net.bettercombat.mixin.RangedWeaponItemMixin).
 */
@Mixin(ProjectileWeaponItem.class)
public class RangedWeaponItemMixin {

    @WrapOperation(
            method = "getHeldProjectile",
            require = 0,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getItemInHand(Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/item/ItemStack;"))
    private static ItemStack bettercombat$getHeldProjectile_Wrapped(
            LivingEntity entity, InteractionHand hand, Operation<ItemStack> original) {
        var originalResult = original.call(entity, hand);
        if (entity instanceof Player player) {
            return InventoryUtil.getOffHandSlotStack(player);
        } else {
            return originalResult;
        }
    }
}
