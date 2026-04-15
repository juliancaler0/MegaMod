package com.ultra.megamod.lib.emf.mixin.mixins.rendering;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.ultra.megamod.lib.emf.models.animation.EMFAnimationEntityContext;
import com.ultra.megamod.lib.emf.models.animation.state.EMFEntityRenderState;
import com.ultra.megamod.lib.etf.features.ETFRenderContext;
import com.ultra.megamod.lib.etf.features.state.ETFEntityRenderState;
import com.ultra.megamod.lib.etf.utils.ETFEntity;
import com.ultra.megamod.lib.emf.utils.EMFEntity;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import com.ultra.megamod.lib.etf.features.state.HoldsETFRenderState;

@Mixin(EntityRenderDispatcher.class)
public abstract class MixinEntityRenderDispatcher {


    private static final String SHADOW_RENDER_ETF =
            "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitShadow(Lcom/mojang/blaze3d/vertex/PoseStack;FLjava/util/List;)V"
            ;

    private static final String RENDER_ETF =
            "submit"
            ;


    @Inject(method = RENDER_ETF,
            at = @At(value = "HEAD"))
    private <S extends net.minecraft.client.renderer.entity.state.EntityRenderState>
    void emf$grabContext(final CallbackInfo ci, @SuppressWarnings("LocalMayBeArgsOnly") @Local S state) {
        EMFAnimationEntityContext.setCurrentEntityIteration((EMFEntityRenderState) ((HoldsETFRenderState) state).etf$getState());
    }



    @Inject(method = RENDER_ETF, at = @At(value = "RETURN"))
    private <S extends net.minecraft.client.renderer.entity.state.EntityRenderState> void emf$endOfRender(
            final CallbackInfo ci
            , @Local(argsOnly = true) S state
    ) {
        EMFEntityRenderState emfState = (EMFEntityRenderState) ((HoldsETFRenderState) state).etf$getState();
        // todo likely extremely broken in 1.21.9
        if (EMFAnimationEntityContext.doAnnounceModels()) {
            EMFAnimationEntityContext.anounceModels(emfState);
        }
    }


    @Inject(method = RENDER_ETF, at = @At(value = "INVOKE", target = SHADOW_RENDER_ETF, shift = At.Shift.BEFORE))
    private void emf$modifyShadowTranslate(final CallbackInfo ci, @Local(argsOnly = true) PoseStack matrices) {
        if (EMFAnimationEntityContext.getShadowX() != 0 || EMFAnimationEntityContext.getShadowZ() != 0) {
            matrices.translate(EMFAnimationEntityContext.getShadowX(), 0, EMFAnimationEntityContext.getShadowZ());
        }
    }

    @Inject(method = RENDER_ETF, at = @At(value = "INVOKE", target = SHADOW_RENDER_ETF, shift = At.Shift.AFTER))
    private void emf$undoModifyShadowTranslate(final CallbackInfo ci, @Local(argsOnly = true) PoseStack matrices) {
        if (EMFAnimationEntityContext.getShadowX() != 0 || EMFAnimationEntityContext.getShadowZ() != 0) {
            matrices.translate(-EMFAnimationEntityContext.getShadowX(), 0, -EMFAnimationEntityContext.getShadowZ());
        }
    }


    @ModifyArg(method = RENDER_ETF, at = @At(value = "INVOKE", target = SHADOW_RENDER_ETF),
            index =
            1
    )
    private float emf$modifyShadowSize(float size) {
        if (!Float.isNaN(EMFAnimationEntityContext.getShadowSize())) {
            return Math.min(size * EMFAnimationEntityContext.getShadowSize(), 32.0F);
        }
        return size;
    }
}
