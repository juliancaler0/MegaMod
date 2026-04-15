package com.ultra.megamod.lib.emf.mixin.mixins.rendering.feature;


import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.entity.layers.SlimeOuterLayer;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.ultra.megamod.lib.emf.models.animation.EMFAnimationEntityContext;

import net.minecraft.client.renderer.rendertype.RenderTypes;

@Mixin(SlimeOuterLayer.class)
public class MixinSlimeOverlayFeatureRenderer {



    @Inject(method =
            "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/SlimeRenderState;FF)V",
            at = @At(value = "HEAD"))
    private void emf$setLayerForOverrides(CallbackInfo ci) {
        EMFAnimationEntityContext.setLayerFactory(
                RenderTypes
                ::entityTranslucent);
    }
}
