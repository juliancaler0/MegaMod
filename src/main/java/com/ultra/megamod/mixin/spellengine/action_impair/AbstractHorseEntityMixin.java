package com.ultra.megamod.mixin.spellengine.action_impair;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import com.ultra.megamod.lib.spellengine.api.effect.EntityActionsAllowed;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractHorse.class)
public class AbstractHorseEntityMixin {
    @Inject(method = "isImmobile", at = @At("HEAD"), cancellable = true)
    private void isImmobile_HEAD_Horse_SpellEngine(CallbackInfoReturnable<Boolean> cir) {
        if (EntityActionsAllowed.isImpaired((LivingEntity) ((Object) this),
                EntityActionsAllowed.Common.MOVE)) {
            cir.setReturnValue(true);
            cir.cancel();
        }
    }
}
