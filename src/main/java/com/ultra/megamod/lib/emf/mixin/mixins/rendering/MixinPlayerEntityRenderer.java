package com.ultra.megamod.lib.emf.mixin.mixins.rendering;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.ultra.megamod.lib.emf.EMF;
import com.ultra.megamod.lib.emf.models.IEMFModel;
import com.ultra.megamod.lib.emf.models.animation.EMFAnimationEntityContext;
import com.ultra.megamod.lib.emf.models.animation.state.EMFEntityRenderState;
import com.ultra.megamod.lib.emf.models.parts.EMFModelPartVanilla;
import com.ultra.megamod.lib.emf.utils.EMFEntity;
import com.ultra.megamod.lib.etf.features.state.ETFEntityRenderState;
import com.ultra.megamod.lib.etf.features.state.HoldsETFRenderState;
import com.ultra.megamod.lib.etf.utils.ETFEntity;


import net.minecraft.client.entity.ClientAvatarEntity;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.world.entity.Avatar;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;

import net.minecraft.client.player.AbstractClientPlayer;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;


@Mixin(AvatarRenderer.class)
public abstract class MixinPlayerEntityRenderer<AvatarlikeEntity extends Avatar & ClientAvatarEntity>
        extends LivingEntityRenderer<AvatarlikeEntity, AvatarRenderState, PlayerModel> {

    public MixinPlayerEntityRenderer() { super(null, null, 0); }



        @Shadow public abstract <AvatarlikeEntity extends Avatar & ClientAvatarEntity> void extractRenderState(final AvatarlikeEntity avatar, final AvatarRenderState avatarRenderState, final float f);


    @Shadow
    public abstract
        AvatarRenderState
    createRenderState();


    @Unique
    private
        AvatarRenderState
    emf$renderState(){
        var state = createRenderState();
        extractRenderState(Minecraft.getInstance().player, state, EMFAnimationEntityContext.getTickDelta());
        return state;
    }




    @Inject(method = "renderHand", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitModelPart(Lnet/minecraft/client/model/geom/ModelPart;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/RenderType;IILnet/minecraft/client/renderer/texture/TextureAtlasSprite;)V"))
    private void emf$setHandAnims(CallbackInfo ci, @Local(argsOnly = true) ModelPart modelPart, @Local(argsOnly = true) PoseStack poseStack
    ) {
        EMFAnimationEntityContext.setCurrentEntityIteration((EMFEntityRenderState) ((HoldsETFRenderState)emf$renderState()).etf$getState());
        EMFAnimationEntityContext.isFirstPersonHand = true;

        // flag this for later submit render
        if (modelPart instanceof EMFModelPartVanilla vanilla) {
            vanilla.isPlayerArm = true;

            // Animate
            if (!EMF.config().getConfig().preventFirstPersonHandAnimating
                    && getModel() instanceof IEMFModel emf && emf.emf$isEMFModel()) {
                emf.emf$getEMFRootModel().triggerManualAnimation(poseStack);
                modelPart.translateAndRotate(poseStack);
            }
        }

    }

    @Inject(method = "renderHand", at = @At(value = "RETURN"))
    private void emf$unsetHand(final CallbackInfo ci) {
        EMFAnimationEntityContext.reset();
    }


}