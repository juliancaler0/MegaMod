package com.ultra.megamod.mixin.archers.screen;

import com.ultra.megamod.feature.combat.archers.item.misc.AutoFireHook;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GrindstoneMenu.class)
public class ArchersGrindstoneMenuMixin {
    @Inject(method = "removeNonCursesFrom", at = @At("HEAD"), cancellable = true)
    private void removeNonCurses_Archers(ItemStack firstInput, CallbackInfoReturnable<ItemStack> cir) {
        if (firstInput != null && !firstInput.isEmpty() && AutoFireHook.isApplied(firstInput)) {
            var newStack = firstInput.copy();
            AutoFireHook.remove(newStack);
            cir.setReturnValue(newStack);
            cir.cancel();
        }
    }
}
