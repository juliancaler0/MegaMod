package com.ultra.megamod.mixin.spellengine.item;

import net.minecraft.world.item.ItemStack;
import com.ultra.megamod.lib.spellengine.api.tags.SpellEngineItemTags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// GrindstoneMenu$2
//
// Anonymous mixin class to modify GrindstoneMenu `new Slot(...) { ... }` instance
// Using `targets = ...` here due to being an anonymous class
// Class name found by clicking into the class, and using `Top menu > View > Show Bytecode`
@Mixin(targets = "net.minecraft.world.inventory.GrindstoneMenu$2")
public class GrindstoneSlotInputMixin {
    @Inject(method = "mayPlace", at = @At("HEAD"), cancellable = true)
    private void canInsert_SpellEngine(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (stack.is(SpellEngineItemTags.GRINDABLE)) {
            cir.setReturnValue(true);
            cir.cancel();
        }
    }
}
