package com.ultra.megamod.lib.etf.compat;



import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.apache.logging.log4j.util.TriConsumer;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.ultra.megamod.lib.etf.utils.ETFUtils2;


/**
 * A separate class to handle a specific sodium injection case for sodium 0.5.6+
 * This is called in one place and handles usage of sodium classes in a quarantined way.
 */
public abstract class SodiumGetBufferInjector {

    private static final TriConsumer<MultiBufferSource, RenderType, VertexConsumer> INSTANCE = get();


    public static void inject(MultiBufferSource provider, RenderType renderLayer, VertexConsumer vertexConsumer) {
        if (INSTANCE != null) INSTANCE.accept(provider, renderLayer, vertexConsumer);
    }

    private static TriConsumer<MultiBufferSource, RenderType, VertexConsumer> get() {
        return null;
    }
}
