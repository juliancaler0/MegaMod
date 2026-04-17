package com.ultra.megamod.lib.owo.ui.renderstate;

import com.ultra.megamod.lib.owo.braid.core.element.BraidBlockElement;
import com.ultra.megamod.lib.owo.braid.core.element.BraidEntityElement;
import com.ultra.megamod.lib.owo.braid.core.element.BraidItemElement;
import net.fabricmc.fabric.api.client.rendering.v1.SpecialGuiElementRegistry;

public class OwoSpecialGuiElementRenderers {
    public static void init() {
        SpecialGuiElementRegistry.register(ctx -> new CubeMapElementRenderState.Renderer(ctx.vertexConsumers()));
        SpecialGuiElementRegistry.register(ctx -> new EntityElementRenderState.Renderer(ctx.vertexConsumers()));
        SpecialGuiElementRegistry.register(ctx -> new BlockElementRenderState.Renderer(ctx.vertexConsumers()));
        SpecialGuiElementRegistry.register(ctx -> new OwoItemElementRenderState.Renderer(ctx.vertexConsumers()));

        SpecialGuiElementRegistry.register(ctx -> new BraidEntityElement.Renderer(ctx.vertexConsumers()));
        SpecialGuiElementRegistry.register(ctx -> new BraidBlockElement.Renderer(ctx.vertexConsumers()));
        SpecialGuiElementRegistry.register(ctx -> new BraidItemElement.Renderer(ctx.vertexConsumers()));
    }
}
