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
        // 1.21.11 ItemRenderer API for standalone model rendering requires the new
        // item model resolution system. For now, spell projectile rendering falls back
        // to the glow quad approach in SpellProjectileRenderer when no model is found.
        // TODO: Adapt to 1.21.11's ItemModelResolver when model-based projectiles are needed
    }
}
