package com.ultra.megamod.mixin.spellengine.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.ultra.megamod.feature.combat.animation.client.SpellSpinContext;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Applies whirlwind-style body rotation around the vertical axis to a player
 * model when the entity is mid-cast on a spell whose {@code animation_spin}
 * field is non-zero. The angle (in degrees) is computed and stashed in
 * {@link SpellSpinContext} during {@code AvatarRendererMixin.extractRenderState}
 * just before this submit() call.
 * <p>
 * Replaces the empty {@code LivingEntityRendererMixin} stub that was left as a
 * 1.21.11 port TODO when the render method's signature changed from
 * {@code render(entity, ...)} to {@code submit(state, poseStack, SubmitNodeCollector, CameraRenderState)}.
 * <p>
 * Targeting {@link LivingEntityRenderer#submit} at HEAD means the rotation is
 * applied to the poseStack while it's still entity-centered (translated to the
 * entity's world position) but before the model's own yaw/setup rotations run.
 * Result: the whole model (body + held items + nametag) rotates around its
 * vertical axis, on top of the natural yaw — matching the Fabric source.
 * <p>
 * Targeting {@link LivingEntityRenderer} (not just AvatarRenderer) is intentional
 * — the rotation is applied via a thread-local that's only populated for player
 * casters by {@code AvatarRendererMixin}, so non-player living entities consume
 * a zero angle and no-op.
 */
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererSpinMixin<S extends LivingEntityRenderState> {

    @Inject(method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V",
            at = @At("HEAD"))
    private void megamod$applySpellSpin(S state, PoseStack poseStack,
                                        SubmitNodeCollector collector,
                                        CameraRenderState camera,
                                        CallbackInfo ci) {
        float angle = SpellSpinContext.consume();
        if (angle == 0f) return;
        poseStack.mulPose(Axis.YP.rotationDegrees(angle));
    }
}
