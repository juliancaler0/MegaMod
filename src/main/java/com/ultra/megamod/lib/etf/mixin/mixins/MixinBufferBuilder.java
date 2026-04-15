package com.ultra.megamod.lib.etf.mixin.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import com.ultra.megamod.lib.etf.features.ETFManager;
import com.ultra.megamod.lib.etf.features.texture_handlers.ETFTexture;
import com.ultra.megamod.lib.etf.utils.ETFRenderLayerWithTexture;
import com.ultra.megamod.lib.etf.utils.ETFVertexConsumer;
import com.mojang.blaze3d.vertex.BufferBuilder;

import java.util.Optional;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;

@Mixin(BufferBuilder.class)
public class MixinBufferBuilder implements ETFVertexConsumer {

    @Unique
    private MultiBufferSource etf$provider = null;
    @Unique
    private RenderType etf$renderLayer = null;
    @Unique
    private ETFTexture etf$ETFTexture = null;

    @Override
    public ETFTexture etf$getETFTexture() {
        return etf$ETFTexture;
    }

    @Override
    public MultiBufferSource etf$getProvider() {
        return etf$provider;
    }

    @Override
    public RenderType etf$getRenderLayer() {
        return etf$renderLayer;
    }

    @Override
    public void etf$initETFVertexConsumer(MultiBufferSource provider, RenderType renderLayer) {
        etf$provider = provider;

        etf$renderLayer = renderLayer;

        //todo sprites give atlas texture here
        if (renderLayer instanceof ETFRenderLayerWithTexture etfRenderLayerWithTexture) {
            Optional<Identifier> possibleId = etfRenderLayerWithTexture.etf$getId();
            possibleId.ifPresent(identifier -> etf$ETFTexture = ETFManager.getInstance().getETFTextureNoVariation(identifier));
        }
//        else {
//            etf$ETFTexture = null;
//        }
    }
}
