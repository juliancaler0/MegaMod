package com.ultra.megamod.mixin.accessories;

import com.ultra.megamod.lib.accessories.api.events.extra.ExtraEventHandler;
import com.ultra.megamod.lib.accessories.fabric.TriState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PiglinAi.class)
public abstract class PiglinAiMixin {

    @Inject(method = "isWearingSafeArmor", at = @At("HEAD"), cancellable = true)
    private static void isWearingGoldAccessory(LivingEntity livingEntity, CallbackInfoReturnable<Boolean> cir){
        var state = ExtraEventHandler.isPiglinsNeutral(livingEntity);

        if(state != TriState.DEFAULT) cir.setReturnValue(state.orElse(false));
    }
}
