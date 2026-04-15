package com.ultra.megamod.lib.emf.mixin.mixins.rendering.submits;

import org.spongepowered.asm.mixin.Mixin;

import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.ultra.megamod.lib.emf.EMF;
import com.ultra.megamod.lib.emf.EMFManager;
import com.ultra.megamod.lib.emf.models.animation.EMFAnimationEntityContext;
import com.ultra.megamod.lib.emf.models.parts.EMFModelPartVanilla;
import com.ultra.megamod.lib.etf.ETF;
import com.ultra.megamod.lib.etf.features.ETFRenderContext;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.feature.ModelPartFeatureRenderer;

@Mixin(ModelPartFeatureRenderer.class)
public class Mixin_ModelPartRenderer {

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;last()Lcom/mojang/blaze3d/vertex/PoseStack$Pose;"))
    private void emf$initRender(final CallbackInfo ci, @Local SubmitNodeStorage.ModelPartSubmit modelSubmit) {
        EMFManager.getInstance().entityRenderCount++;
        var light = modelSubmit.lightCoords();
        if (light == ETF.EMISSIVE_FEATURE_LIGHT_VALUE || light == EMF.EYES_FEATURE_LIGHT_VALUE) {
            ETFRenderContext.startSpecialRenderOverlayPhase();
        } else {
            ETFRenderContext.endSpecialRenderOverlayPhase();
        }

        if (modelSubmit.modelPart() instanceof EMFModelPartVanilla vanilla && vanilla.isPlayerArm) {
            EMFAnimationEntityContext.isFirstPersonHand = true;
        }
    }

    @Inject(method = "render", at = @At(value = "TAIL"))
    private void emf$endRender(final CallbackInfo ci) {
        ETFRenderContext.endSpecialRenderOverlayPhase();
        EMFAnimationEntityContext.isFirstPersonHand = false;
    }

}