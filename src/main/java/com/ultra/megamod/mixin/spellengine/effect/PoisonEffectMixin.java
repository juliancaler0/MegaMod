package com.ultra.megamod.mixin.spellengine.effect;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.PoisonMobEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PoisonMobEffect.class)
public class PoisonEffectMixin {
    // applyEffectTick signature changed in 1.21.11 to (ServerLevel, LivingEntity, int)
    // The WrapOperation on damage call is removed since the method signature changed significantly.
    // Poison amplification is handled through SpellEngine's effect system instead.

    @Inject(method = "shouldApplyEffectTickThisTick", at = @At("HEAD"), cancellable = true, require = 0)
    private void canApplyUpdateEffect_SpellEngine(int duration, int amplifier, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(duration % 25 == 0);
        cir.cancel();
    }
}
