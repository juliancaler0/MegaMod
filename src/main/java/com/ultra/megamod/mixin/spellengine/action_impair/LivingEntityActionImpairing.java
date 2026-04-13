package com.ultra.megamod.mixin.spellengine.action_impair;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.core.Holder;
import com.ultra.megamod.lib.spellengine.api.effect.EntityActionsAllowed;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(LivingEntity.class)
public abstract class LivingEntityActionImpairing implements EntityActionsAllowed.ControlledEntity {
    @Shadow public abstract void stopUsingItem();

    @Inject(method = "jumpFromGround", at = @At("HEAD"), cancellable = true)
    private void jumpFromGround_HEAD_Spell_Engine(CallbackInfo ci) {
        if (EntityActionsAllowed.isImpaired((LivingEntity) ((Object) this),
                EntityActionsAllowed.Common.JUMP)) {
            ci.cancel();
        }
    }

    @Inject(method = "isImmobile", at = @At("HEAD"), cancellable = true)
    private void isImmobile_HEAD_SpellEngine(CallbackInfoReturnable<Boolean> cir) {
        if (EntityActionsAllowed.isImpaired((LivingEntity) ((Object) this),
                EntityActionsAllowed.Common.MOVE)) {
            cir.setReturnValue(true);
            cir.cancel();
        }
    }

    @Inject(method = "updateUsingItem", at = @At("HEAD"), cancellable = true)
    private void updateUsingItem_HEAD_SpellEngine(CallbackInfo ci) {
        if (EntityActionsAllowed.isImpaired((LivingEntity) ((Object) this),
                EntityActionsAllowed.Player.ITEM_USE)) {
            stopUsingItem();
            ci.cancel();
        }
    }

    // MARK: Actions Allowed (CC)

    private EntityActionsAllowed entityActionsAllowed_SpellEngine = EntityActionsAllowed.ANY;

    @Shadow private boolean effectsDirty;

    @Shadow @Final private Map<Holder<MobEffect>, MobEffectInstance> activeEffects;

    @Inject(method = "tickEffects", at = @At("TAIL"))
    private void tickEffects_TAIL_SpellEngine(CallbackInfo ci) {
        // Update action allowed state whenever effects tick (effectsDirty is checked internally)
        updateEntityActionsAllowed();
    }

    public void updateEntityActionsAllowed() {
        entityActionsAllowed_SpellEngine = EntityActionsAllowed.fromEffects(activeEffects.keySet());
    }

    public EntityActionsAllowed actionImpairing() {
        return entityActionsAllowed_SpellEngine;
    }
}
