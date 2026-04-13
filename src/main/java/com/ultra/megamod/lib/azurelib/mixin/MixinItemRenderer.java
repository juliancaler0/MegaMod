/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright (c) 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package com.ultra.megamod.lib.azurelib.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.ultra.megamod.lib.azurelib.common.render.item.AzItemRendererRegistry;
import com.ultra.megamod.lib.azurelib.common.render.item.AzItemRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Render hook to inject AzureLib's custom item rendering.
 * In 1.21.11, ItemRenderer.render() was removed. The new pipeline uses
 * ItemModelResolver to populate ItemStackRenderState, which is then submitted.
 * We hook appendItemLayers to intercept items that have AzureLib renderers registered.
 */
@Mixin(ItemModelResolver.class)
public class MixinItemRenderer {

    @Inject(
        method = "appendItemLayers",
        at = @At("HEAD"),
        cancellable = true
    )
    private void itemModelHook(
        ItemStackRenderState renderState,
        ItemStack itemStack,
        ItemDisplayContext displayContext,
        Level level,
        ItemOwner itemOwner,
        int seed,
        CallbackInfo ci
    ) {
        var item = itemStack.getItem();
        var renderer = AzItemRendererRegistry.getOrNull(item);

        if (renderer != null) {
            // AzureLib item detected: render using the legacy MultiBufferSource pipeline.
            // We mark the render state as animated and cancel the normal model resolution,
            // since AzureLib handles its own model/texture/animation pipeline.
            renderState.setAnimated();

            var mc = Minecraft.getInstance();
            var bufferSource = mc.renderBuffers().bufferSource();
            var poseStack = new PoseStack();

            switch (displayContext) {
                case GUI -> renderer.renderByGui(itemStack, displayContext, poseStack, bufferSource, 0xF000F0);
                default -> renderer.renderByItem(itemStack, displayContext, poseStack, bufferSource, 0xF000F0);
            }
            bufferSource.endBatch();

            ci.cancel();
        }
    }
}
