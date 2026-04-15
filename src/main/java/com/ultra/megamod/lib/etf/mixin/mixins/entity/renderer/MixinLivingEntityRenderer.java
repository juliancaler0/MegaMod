package com.ultra.megamod.lib.etf.mixin.mixins.entity.renderer;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.ultra.megamod.lib.etf.features.ETFRenderContext;
import com.ultra.megamod.lib.etf.features.state.ETFEntityRenderState;
import com.ultra.megamod.lib.etf.features.state.HoldsETFRenderState;
import com.ultra.megamod.lib.etf.utils.ETFEntity;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
@Mixin(LivingEntityRenderer.class)
public abstract class MixinLivingEntityRenderer<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>> extends EntityRenderer<T, S> {


    @SuppressWarnings("unused")
    protected MixinLivingEntityRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);

    }

    private static final String RENDER = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V";

    @Inject(method = RENDER, at = @At(value = "INVOKE", target = "Ljava/util/List;iterator()Ljava/util/Iterator;"))
    private void etf$markFeatures(CallbackInfo ci, @Share("shareState") LocalRef<ETFEntityRenderState> etf$heldEntity
               , @Local(argsOnly = true) net.minecraft.client.renderer.entity.state.LivingEntityRenderState state
            ) { etf$heldEntity.set(((HoldsETFRenderState) state).etf$getState());
        ETFRenderContext.allowRenderLayerTextureModify();
        ETFRenderContext.setRenderingFeatures(true);
    }

    @Inject(method = RENDER, at = @At(value = "INVOKE", target = "Ljava/util/Iterator;next()Ljava/lang/Object;"))
    private void etf$markFeaturesLoopEnd(CallbackInfo ci, @Share("shareState") LocalRef<ETFEntityRenderState> etf$heldEntity) {
        // assert main entity each loop in case of other entities within feature renderer
        ETFRenderContext.setCurrentEntity(etf$heldEntity.get());
        ETFRenderContext.allowRenderLayerTextureModify();
        ETFRenderContext.endSpecialRenderOverlayPhase();
    }

    @Inject(method = RENDER, at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V"))
    private void etf$markFeaturesEnd(CallbackInfo ci) {
        ETFRenderContext.setRenderingFeatures(false);
    }


}


