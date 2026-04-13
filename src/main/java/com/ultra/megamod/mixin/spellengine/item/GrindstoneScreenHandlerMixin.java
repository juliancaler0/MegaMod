package com.ultra.megamod.mixin.spellengine.item;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.inventory.GrindstoneMenu;
import com.ultra.megamod.lib.spellengine.api.tags.SpellEngineItemTags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GrindstoneMenu.class)
public class GrindstoneScreenHandlerMixin {
    @Inject(method = "computeResult", at = @At("HEAD"), cancellable = true)
    private void computeResult_SpellEngine(ItemStack firstInput, ItemStack secondInput, CallbackInfoReturnable<ItemStack> cir) {
        if (firstInput.is(SpellEngineItemTags.GRINDABLE) && secondInput.isEmpty()) {
            cir.setReturnValue(new ItemStack(Items.PAPER, 1));
            cir.cancel();
        }
    }
}
