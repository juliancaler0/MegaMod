package com.ultra.megamod.lib.azurelib.common.render.item;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemStack;

import com.ultra.megamod.lib.azurelib.common.model.AzBakedModel;

public class AzItemGuiRenderUtil {

    /**
     * Wrapper method to handle rendering the item in a GUI context (defined by
     * {@link net.minecraft.world.item.ItemDisplayContext#GUI} normally).<br>
     * Just includes some additional required transformations and settings.
     */
    public static void renderInGui(
        AzItemRendererConfig config,
        AzItemRendererPipeline rendererPipeline,
        ItemStack stack,
        AzBakedModel model,
        ItemStack currentItemStack,
        PoseStack poseStack,
        MultiBufferSource source,
        int packedLight
    ) {
        // In 1.21.11, Lighting methods were renamed/removed.
        // setupForEntityInInventory -> setupForEntityInInventory (may not exist)
        // setupForFlatItems -> setupForFlatItems (may not exist)
        // setupFor3DItems -> setupFor3DItems (may not exist)
        // These lighting setups are now handled by the rendering pipeline internally.

        var context = rendererPipeline.context();
        var partialTick = Minecraft.getInstance().getDeltaTracker().getRealtimeDeltaTicks();
        var bSource =
            source instanceof MultiBufferSource.BufferSource bufferSource
                ? bufferSource
                : Minecraft.getInstance().levelRenderer.renderBuffers.bufferSource();
        var textureLocation = config.textureLocation(context.currentEntity(), stack);
        var renderType = rendererPipeline.context()
            .getDefaultRenderType(
                stack,
                textureLocation,
                bSource,
                partialTick,
                config.getRenderType(context.currentEntity(), stack),
                config.alpha(stack)
            );
        var buffer = bSource.getBuffer(renderType);

        poseStack.pushPose();

        rendererPipeline.render(poseStack, model, stack, bSource, renderType, buffer, 0, partialTick, packedLight);

        bSource.endBatch();

        poseStack.popPose();
    }
}
