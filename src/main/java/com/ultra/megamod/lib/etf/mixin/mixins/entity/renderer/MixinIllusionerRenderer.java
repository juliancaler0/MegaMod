package com.ultra.megamod.lib.etf.mixin.mixins.entity.renderer;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.entity.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.ultra.megamod.lib.etf.features.ETFRenderContext;
import com.ultra.megamod.lib.etf.features.state.ETFEntityRenderState;
import com.ultra.megamod.lib.etf.features.state.HoldsETFRenderState;
import com.ultra.megamod.lib.etf.utils.ETFEntity;


@Mixin(IllusionerRenderer.class)
public abstract class MixinIllusionerRenderer {

    @Unique
    private ETFEntityRenderState etf$heldEntity = null;

    private static final String RENDER = "submit(Lnet/minecraft/client/renderer/entity/state/IllusionerRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V";

    @Inject(method = RENDER, at = @At(value = "HEAD"))
    private void etf$start(CallbackInfo ci
               , @Local(argsOnly = true) net.minecraft.client.renderer.entity.state.IllusionerRenderState state
        ) { ETFRenderContext.setCurrentEntity(((HoldsETFRenderState) state).etf$getState());
    }

    @Inject(method = RENDER, at = @At(value = "INVOKE", target =
                    "Lnet/minecraft/client/renderer/entity/IllagerRenderer;submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V"
                    ))
    private void etf$loop(CallbackInfo ci) {
        //assert main entity each loop
        ETFRenderContext.setCurrentEntity(etf$heldEntity);
        ETFRenderContext.allowRenderLayerTextureModify();
        ETFRenderContext.endSpecialRenderOverlayPhase();
    }
}


