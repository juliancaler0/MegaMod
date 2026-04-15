package com.ultra.megamod.lib.emf.mixin.mixins.rendering.feature;

import net.minecraft.client.renderer.entity.layers.EyesLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import static com.ultra.megamod.lib.emf.EMF.EYES_FEATURE_LIGHT_VALUE;

@Mixin(EyesLayer.class)
public class MixinEyesFeatureRenderer {
    @SuppressWarnings("SameReturnValue")
    @ModifyVariable(method =
            "submit"
            , at = @At(value = "HEAD"), argsOnly = true, ordinal = 0)
    private int emf$markEyeLight(int i) {
        return EYES_FEATURE_LIGHT_VALUE;
    }

}
