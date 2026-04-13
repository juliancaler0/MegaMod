package com.ultra.megamod.mixin.rangedweapon.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.ultra.megamod.lib.rangedweapon.api.CustomBow;
import com.ultra.megamod.lib.rangedweapon.api.CustomCrossbow;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AvatarRenderer.class)
public class RangedWeaponAvatarRendererMixin {
    /**
     * Arm pose `CROSSBOW_HOLD` for any custom crossbow, `BOW_AND_ARROW` for any custom bow.
     * In 1.21.11, AvatarRenderer.getArmPose checks ItemStack.is(Items.CROSSBOW) for CROSSBOW_HOLD pose.
     */

    @WrapOperation(
            method = "getArmPose(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/client/model/HumanoidModel$ArmPose;",
            require = 0, // NeoForge may alter the method structure
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z")
    )
    private static boolean armPose_crossbowHold_RWA(ItemStack itemStack, Item item, Operation<Boolean> original) {
        if (item == Items.CROSSBOW) {
            if (CustomCrossbow.instances.contains(itemStack.getItem())) {
                return true;
            }
        }
        if (item == Items.BOW) {
            if (CustomBow.instances.contains(itemStack.getItem())) {
                return true;
            }
        }
        return original.call(itemStack, item);
    }
}
