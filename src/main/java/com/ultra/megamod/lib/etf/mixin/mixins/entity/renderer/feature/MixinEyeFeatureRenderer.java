package com.ultra.megamod.lib.etf.mixin.mixins.entity.renderer.feature;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import com.ultra.megamod.lib.etf.features.ETFRenderContext;
import com.ultra.megamod.lib.etf.utils.ETFRenderLayerWithTexture;
import com.ultra.megamod.lib.etf.utils.ETFUtils2;

@Mixin(EyesLayer.class)
public abstract class MixinEyeFeatureRenderer {

    @ModifyExpressionValue(method = "submit", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/layers/EyesLayer;renderType()Lnet/minecraft/client/renderer/RenderType;"))
    private RenderType etf$allowModifiableEyes(RenderType layer) {
        //the eye texture render layers are hard coded in vanilla and do not recalculate each time
        if (layer instanceof ETFRenderLayerWithTexture etf && etf.etf$getId().isPresent()) {
            Identifier id = etf.etf$getId().get();
            Identifier variant = ETFUtils2.getETFVariantNotNullForInjector(id);
            if (!id.equals(variant)) {
                //if there is a variant then lets send a layer with it
                boolean allowed = ETFRenderContext.isAllowedToRenderLayerTextureModify();
                ETFRenderContext.preventRenderLayerTextureModify();

                RenderType layer2 =
                        net.minecraft.client.renderer.rendertype.RenderTypes
                                .eyes(variant);

                if (allowed) ETFRenderContext.allowRenderLayerTextureModify();

                return layer2;
            }
        }
        //no need to variate so lets just send the hard coded final layer
        return layer;
    }

}


