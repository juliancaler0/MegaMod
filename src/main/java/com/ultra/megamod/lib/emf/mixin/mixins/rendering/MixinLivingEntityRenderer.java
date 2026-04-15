package com.ultra.megamod.lib.emf.mixin.mixins.rendering;


import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.ultra.megamod.lib.emf.EMF;
import com.ultra.megamod.lib.emf.config.EMFConfig;
import com.ultra.megamod.lib.emf.models.animation.state.EMFBipedPose;
import com.ultra.megamod.lib.emf.models.animation.state.EMFEntityRenderState;
import com.ultra.megamod.lib.emf.models.parts.EMFModelPartRoot;
import com.ultra.megamod.lib.emf.models.IEMFModel;
import com.ultra.megamod.lib.emf.models.animation.EMFAnimationEntityContext;
import com.ultra.megamod.lib.emf.EMFManager;
import com.ultra.megamod.lib.emf.utils.EMFEntity;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import com.ultra.megamod.lib.etf.features.state.HoldsETFRenderState;

@Mixin(LivingEntityRenderer.class)
public abstract class MixinLivingEntityRenderer<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>> extends EntityRenderer<T, S> implements RenderLayerParent<S, M> {

    @Shadow
    public abstract Identifier getTextureLocation(final S livingEntityRenderState);



    @Shadow
    protected M model;

    @Shadow
    public abstract M getModel();

    @SuppressWarnings("unused")
    protected MixinLivingEntityRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Inject(method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/EntityModel;setupAnim(Ljava/lang/Object;)V",
                    shift = At.Shift.AFTER))
    private void falseAnimation(CallbackInfo ci, @Local(argsOnly = true) PoseStack pose, @Local(argsOnly = true) S renderState) {
        // animate so that dependant layers can read the positions (only applies if they set their matrix prior to submission)
        IEMFModel model = (IEMFModel) getModel();
        if (model.emf$isEMFModel() && model.emf$getEMFRootModel().hasAnimation()) {
            model.emf$getEMFRootModel().triggerManualAnimation(pose);
            // Store the biped pose in the render state for use by layers that need it.
            if (getModel() instanceof HumanoidModel<?> humanoidModel) {
                var state = (EMFEntityRenderState) ((HoldsETFRenderState) renderState).etf$getState();
                if (state != null) state.setBipedPose(new EMFBipedPose(humanoidModel));
            }

        }
    }





    @ModifyExpressionValue(method = "getRenderType", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;getTextureLocation(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;)Lnet/minecraft/resources/Identifier;"
    ))
    private Identifier emf$getTextureRedirect(final Identifier original){

        if (((IEMFModel) model).emf$isEMFModel()) {
            EMFModelPartRoot root = ((IEMFModel) model).emf$getEMFRootModel();
            if (root != null) {
                Identifier texture = root.getTopLevelJemTexture();
                if (texture != null)
                    return texture;
            }
        }

        return original;

    }

    private static final String RENDER = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V";


    @Inject(method = RENDER, at = @At(value = "INVOKE", target = "Ljava/util/List;iterator()Ljava/util/Iterator;"))
    private void emf$grabEntity(CallbackInfo ci, @Share("iteration") LocalRef<EMFAnimationEntityContext.IterationContext> emf$heldIteration) {
        emf$heldIteration.set(EMFAnimationEntityContext.getIterationContext());
    }

    @Inject(method = RENDER, at = @At(value = "INVOKE", target = "Ljava/util/Iterator;next()Ljava/lang/Object;"))
    private void emf$eachFeatureLoop(CallbackInfo ci, @Share("iteration") LocalRef<EMFAnimationEntityContext.IterationContext> emf$heldIteration) {
        if (emf$heldIteration.get() != null && EMFManager.getInstance().entityRenderCount != emf$heldIteration.get().entityRenderCount()) {
            EMFAnimationEntityContext.setIterationContext(emf$heldIteration.get());
        }
        //todo needed for stray bogged drowned outer layers in 1.21.2+
        //check its needed for 1.21.1
        EMFManager.getInstance().entityRenderCount++;
    }

}
