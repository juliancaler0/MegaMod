package com.ultra.megamod.lib.emf.mixin.mixins.rendering.feature;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.world.entity.HumanoidArm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.ultra.megamod.lib.emf.models.animation.EMFAnimationEntityContext;
import com.ultra.megamod.lib.emf.models.animation.EMFAttachments;
import com.ultra.megamod.lib.emf.models.animation.state.EMFEntityRenderState;
import com.ultra.megamod.lib.etf.features.state.HoldsETFRenderState;

import com.ultra.megamod.lib.emf.models.IEMFModel;
import com.llamalad7.mixinextras.injector.ModifyReceiver;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;


@Mixin(ItemInHandLayer.class)
public class
MixinHeldItemFeatureRenderer<S extends ArmedEntityRenderState, M extends EntityModel<S> & ArmedModel>
{

    private static final String RENDER_ARM = "submitArmWithItem";

    @Inject(method = RENDER_ARM,
            at = @At(value = "HEAD"))
    private void emf$setHand(final CallbackInfo ci,
                             @Local(argsOnly = true) S stateIn,
                             @Local HumanoidArm arm, @Share("armOverride") LocalRef<EMFAttachments> armOverride) {
        EMFAnimationEntityContext.setInHand = true;
        if (stateIn == null) return;
        var state = (EMFEntityRenderState) ((HoldsETFRenderState) stateIn).etf$getState();
        if (state == null) return;

        armOverride.set(arm == HumanoidArm.RIGHT ? state.rightArmOverride() : state.leftArmOverride());
    }

    @Inject(
            method = RENDER_ARM,
            at = @At(value = "INVOKE", target =
                    "Lnet/minecraft/client/model/ArmedModel;translateToHand(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lnet/minecraft/world/entity/HumanoidArm;Lcom/mojang/blaze3d/vertex/PoseStack;)V",
                    shift = At.Shift.AFTER)
    )
    private void emf$transforms(final CallbackInfo ci, @Local(argsOnly = true) PoseStack matrices,
                                @Share("armOverride") LocalRef<EMFAttachments> armOverride,
                                @Share("needsPop") LocalBooleanRef needsToPop) {
        if (armOverride.get() != null) {
            var entry = armOverride.get().pose;
            if (entry != null) {
                needsToPop.set(true);

                matrices.pushPose();
                matrices.last().set(entry);
            }
        }
    }

    @Inject(method = RENDER_ARM,
            at = @At(value = "TAIL"))
    private void emf$unsetHand(final CallbackInfo ci, @Local(argsOnly = true) PoseStack matrices, @Share("needsPop") LocalBooleanRef needsToPop) {
        EMFAnimationEntityContext.setInHand = false;
        if (needsToPop.get()) matrices.popPose();
    }

}