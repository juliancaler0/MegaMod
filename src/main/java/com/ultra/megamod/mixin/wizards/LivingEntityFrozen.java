package com.ultra.megamod.mixin.wizards;

import com.ultra.megamod.feature.combat.spell.SpellEffects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityFrozen {
    @Shadow
    public abstract boolean hasEffect(Holder<MobEffect> effect);

    @Inject(method = "baseTick", at = @At("TAIL"))
    public void baseTick_TAIL_FrozenByStatusEffect(CallbackInfo ci) {
        var entity = (Entity) ((Object) this);
        if (hasEffect(SpellEffects.FROZEN)) {
            entity.setIsInPowderSnow(true);
        }
    }

    @Inject(method = "jumpFromGround", at = @At("HEAD"), cancellable = true)
    public void jumpFromGround_HEAD_NoJumpingWhileFrozen(CallbackInfo ci) {
        if (hasEffect(SpellEffects.FROZEN)) {
            ci.cancel();
        }
    }
}
