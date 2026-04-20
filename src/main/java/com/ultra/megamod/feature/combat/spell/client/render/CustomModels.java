package com.ultra.megamod.feature.combat.spell.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Utility for rendering item models on custom render layers.
 * Ported from SpellEngine. Adapted for NeoForge 1.21.11.
 */
public class CustomModels {

    public static void render(RenderType renderLayer, Identifier modelId,
                              PoseStack matrices, MultiBufferSource bufferSource, int light, int seed) {
        // Try to find as a registered item
        var itemOpt = BuiltInRegistries.ITEM.getOptional(modelId);
        if (itemOpt.isPresent()) {
            var stack = new ItemStack(itemOpt.get());
            if (!stack.isEmpty()) {
                renderItemStack(matrices, bufferSource, light, stack);
                return;
            }
        }
    }

    public static void renderItemStack(PoseStack matrices, MultiBufferSource bufferSource,
                                        int light, ItemStack stack) {
        // Unreachable since SpellProjectileRenderer was rewritten against 1.21.11's
        // {@code submit(SubmitNodeCollector)} API — it now resolves the model through
        // {@code ItemModelResolver.updateForTopItem} and submits via
        // {@code ItemStackRenderState.submit} directly, bypassing this MultiBufferSource path.
        // Kept as an empty shim so any legacy call sites still compile; delete once
        // confirmed nothing else references {@link CustomModels#render}.
    }
}
