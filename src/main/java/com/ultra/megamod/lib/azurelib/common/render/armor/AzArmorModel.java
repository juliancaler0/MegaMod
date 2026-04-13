package com.ultra.megamod.lib.azurelib.common.render.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;

public class AzArmorModel extends HumanoidModel<HumanoidRenderState> {

    private static boolean azureBufferLogged = false;

    private final AzArmorRendererPipeline rendererPipeline;

    public AzArmorModel(AzArmorRendererPipeline rendererPipeline) {
        super(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.PLAYER));
        this.rendererPipeline = rendererPipeline;
    }

    /**
     * Renders the 3D geo armor model into the supplied VertexConsumer.
     * Designed to be called from inside a {@link net.minecraft.client.renderer.SubmitNodeCollector#submitCustomGeometry}
     * lambda. The buffer provided is the vertex consumer for the correct RenderType already resolved by the collector.
     */
    public void renderAzureBuffer(
        PoseStack poseStack,
        VertexConsumer buffer,
        int packedLight,
        int packedOverlay,
        int color
    ) {
        var mc = Minecraft.getInstance();
        var context = rendererPipeline.context();
        var currentEntity = context.currentEntity();
        var currentStack = context.currentStack();

        // Guard: entity context must be set (populated by prepForRender)
        if (currentEntity == null || currentStack == null || buffer == null) {
            if (!azureBufferLogged) {
                azureBufferLogged = true;
                com.ultra.megamod.MegaMod.LOGGER.warn("[AzArmor] renderAzureBuffer early exit: entity={} stack={} buffer={}",
                        currentEntity, currentStack, buffer);
            }
            return;
        }

        if (!azureBufferLogged) {
            azureBufferLogged = true;
            com.ultra.megamod.MegaMod.LOGGER.info("[AzArmor] renderAzureBuffer OK: entity={} stack={}", currentEntity.getName().getString(), currentStack.getItem());
        }

        var config = rendererPipeline.config();
        var animatable = context.animatable();
        var partialTick = mc.getDeltaTracker().getRealtimeDeltaTicks();
        var textureLocation = config.textureLocation(currentEntity, animatable);

        // Build a render type matching the armor texture
        RenderType renderType = config.getRenderType(currentEntity, animatable);
        if (renderType == null) {
            renderType = RenderTypes.entityCutoutNoCull(textureLocation);
        }

        // Wrap the caller's VertexConsumer as a MultiBufferSource so AzureLib's pipeline
        // writes all its vertex output to the buffer provided by SubmitNodeCollector.
        // The collector is already positioned correctly in the frame's render order for this armor piece.
        final VertexConsumer capturedBuffer = buffer;
        final RenderType capturedType = renderType;
        MultiBufferSource bufferSource = type -> capturedBuffer;

        var model = rendererPipeline.renderer().provider().provideBakedModel(currentEntity, animatable);

        if (model == null) {
            com.ultra.megamod.MegaMod.LOGGER.warn("[AzArmor] Baked model is NULL for {} — geo JSON not loaded?", currentStack.getItem());
            return;
        }

        try {
            rendererPipeline.render(poseStack, model, animatable, bufferSource, capturedType, capturedBuffer, 0, partialTick, packedLight);
        } catch (Throwable t) {
            com.ultra.megamod.MegaMod.LOGGER.error("[AzArmor] rendererPipeline.render threw: {}", t.getMessage(), t);
        }
    }

    /**
     * Legacy no-op (pose transfer is now done via setupAnim on the render state).
     * In 1.21.11, HumanoidModel fields like young/crouching/riding/armPose were removed;
     * state is managed via HumanoidRenderState.
     */
    public void applyBaseModel(HumanoidModel<?> baseModel) {
        // No-op in 1.21.11
    }

    @Override
    public void setAllVisible(boolean pVisible) {
        super.setAllVisible(pVisible);
        var boneContext = rendererPipeline.context().boneContext();
        boneContext.setAllVisible(pVisible);
    }
}
