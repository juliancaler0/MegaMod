package com.ultra.megamod.lib.etf.utils;

import com.ultra.megamod.lib.etf.features.texture_handlers.ETFTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.jetbrains.annotations.Nullable;

/**
 * Duck-typed interface attached to {@code VertexConsumer}/{@code BufferBuilder} (via
 * {@code MixinBufferBuilder}) so the {@link com.ultra.megamod.lib.etf.mixin.MixinModelPart}
 * emissive/enchant overlay pass can retrieve the back-reference to the
 * {@link MultiBufferSource} + {@link RenderType} + {@link ETFTexture} used to build this
 * consumer.
 * <p>
 * Ported 1:1 from upstream ETF's {@code ETFVertexConsumer}.
 */
public interface ETFVertexConsumer {

    @Nullable
    default ETFTexture etf$getETFTexture() {
        return null;
    }

    default void etf$setETFTexture(@Nullable ETFTexture texture) {}

    @Nullable
    default MultiBufferSource etf$getProvider() {
        return null;
    }

    default void etf$setProvider(@Nullable MultiBufferSource provider) {}

    @Nullable
    default RenderType etf$getRenderLayer() {
        return null;
    }

    default void etf$setRenderLayer(@Nullable RenderType renderLayer) {}
}
