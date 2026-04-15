package com.ultra.megamod.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.ultra.megamod.feature.adminmodules.AdminModuleState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.state.ItemEntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin into ItemEntityRenderer to implement:
 * - ItemPhysics: make dropped items render flat on the ground instead of
 *   floating and spinning.
 *
 * In 1.21.11, rendering uses the extractRenderState + submit pipeline.
 * We inject into submit() to apply rotation transforms before vanilla rendering.
 */
@Mixin(value = ItemEntityRenderer.class, priority = 1100)
public class ItemEntityRendererMixin {

    /**
     * Tracks whether pushPose was actually called in the HEAD injection,
     * so the RETURN injection can reliably match it with popPose even if
     * the module state changes between the two injections.
     */
    private static final ThreadLocal<Boolean> megamod$posePushed = ThreadLocal.withInitial(() -> false);

    /**
     * ItemPhysics: Before the item is rendered via submit(), apply a rotation
     * to make it lay flat on the ground. We rotate 90 degrees around X to
     * flatten it, then translate down slightly so it sits on the surface.
     */
    @Inject(method = "submit(Lnet/minecraft/client/renderer/entity/state/ItemEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V",
            at = @At("HEAD"))
    private void megamod$flattenItem(ItemEntityRenderState renderState, PoseStack poseStack,
                                      SubmitNodeCollector collector, CameraRenderState cameraState,
                                      CallbackInfo ci) {
        if (!AdminModuleState.itemPhysicsEnabled) {
            megamod$posePushed.set(false);
            return;
        }

        megamod$posePushed.set(true);

        // Apply transforms to make the item lay flat
        poseStack.pushPose();
        // Rotate 90 degrees around X axis so the item faces up
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0f));
        // Slight Y offset down to sit on the ground plane
        poseStack.translate(0.0, -0.15, -0.15);
        // Add a per-entity rotation for variety using the seed
        float yRotation = (renderState.seed * 137) % 360;
        poseStack.mulPose(Axis.ZP.rotationDegrees(yRotation));
    }

    /**
     * Pop the extra pose we pushed for flat items after rendering completes.
     * Uses the tracked flag instead of re-checking module state to avoid
     * PoseStack imbalance if the flag changed between HEAD and RETURN.
     */
    @Inject(method = "submit(Lnet/minecraft/client/renderer/entity/state/ItemEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V",
            at = @At("RETURN"))
    private void megamod$restoreStack(ItemEntityRenderState renderState, PoseStack poseStack,
                                       SubmitNodeCollector collector, CameraRenderState cameraState,
                                       CallbackInfo ci) {
        if (!megamod$posePushed.get()) return;
        megamod$posePushed.set(false);

        poseStack.popPose();
    }
}
