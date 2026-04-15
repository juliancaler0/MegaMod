package com.ultra.megamod.lib.emf.mixin.mixins.rendering.model;

import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.ultra.megamod.lib.emf.models.animation.EMFAnimationEntityContext;

@Mixin(VillagerModel.class)
public abstract class MixinVillagerModel {

    @Inject(method =
            "setupAnim(Lnet/minecraft/client/renderer/entity/state/VillagerRenderState;)V"
            , at = @At(value = "HEAD"))
    private void emf$assertLayerFactory(final CallbackInfo ci) {
        EMFAnimationEntityContext.setLayerFactory(
                net.minecraft.client.renderer.rendertype.RenderTypes
                        ::entityCutoutNoCull);
    }



}
