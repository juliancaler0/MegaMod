package com.ultra.megamod.lib.spellengine.api.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import com.ultra.megamod.lib.spellengine.client.render.CustomModelRegistry;

import java.util.List;

public class CustomModels {
    /// Still used by Quiver renderer registration,
    /// should only be removed past 1.21.6
    @Deprecated(forRemoval = true)
    public static void registerModelIds(List<Identifier> ids) {
        CustomModelRegistry.modelIds.addAll(ids);
    }

    public static void render(RenderType renderType, ItemRenderer itemRenderer, Identifier modelId,
                              PoseStack matrices, MultiBufferSource vertexConsumers, int light, int seed) {
        var itemOpt = BuiltInRegistries.ITEM.get(modelId);
        if (itemOpt.isPresent()) {
            var stack = itemOpt.get().value().getDefaultInstance();
            if (!stack.isEmpty()) {
                renderItemStack(stack, matrices, vertexConsumers, light, seed);
            }
        }
    }

    public static void renderItemStack(ItemStack stack, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int seed) {
        // In 1.21.11, item rendering uses SubmitNodeCollector instead of MultiBufferSource.
        // This method is a compatibility stub - callers using the new renderer should use
        // ItemStackRenderState.submit() directly with a SubmitNodeCollector.
        var mc = Minecraft.getInstance();
        var renderState = new ItemStackRenderState();
        mc.getItemModelResolver().updateForTopItem(renderState, stack, ItemDisplayContext.FIXED, mc.level, null, seed);
        matrices.translate(-0.5, -0.5, -0.5);
        // Note: actual rendering requires SubmitNodeCollector, not MultiBufferSource
        // This stub preserves API compatibility but won't render until callers are updated
    }

    public static void renderModel(RenderType renderType, Object itemRenderer,
                                    PoseStack matrices, MultiBufferSource vertexConsumers, int light, Object model) {
        // Legacy method stub - rendering now uses ItemStackRenderState
        // This method is kept for API compatibility but does nothing
    }
}
