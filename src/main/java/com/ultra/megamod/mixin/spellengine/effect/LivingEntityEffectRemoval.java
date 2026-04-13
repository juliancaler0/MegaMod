package com.ultra.megamod.mixin.spellengine.effect;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectInstance;
import com.ultra.megamod.lib.spellengine.api.effect.OnRemoval;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;

@Mixin(LivingEntity.class)
public class LivingEntityEffectRemoval {
    @Inject(method = "onEffectsRemoved", at = @At("TAIL"))
    private void onEffectsRemoved_TAIL_SpellEngine(Collection<MobEffectInstance> effects, CallbackInfo ci) {
        var entity = (LivingEntity) (Object) this;
        for (var effectInstance : effects) {
            var effect = effectInstance.getEffect().value();
            if (effect instanceof OnRemoval onRemoval) {
                var handler = onRemoval.removalHandler();
                if (handler != null) {
                    handler.accept(new OnRemoval.Context(entity));
                }
            }
        }
    }
}
