package com.ultra.megamod.mixin.spellengine.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.ultra.megamod.feature.combat.animation.client.SpellSpinContext;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Applies whirlwind-style body rotation around the vertical axis to a player
 * model when the entity is mid-cast on a spell whose {@code animation_spin}
 * field is non-zero. The angle (in degrees) is computed and stashed in
 * {@link SpellSpinContext} during {@code AvatarRendererMixin.extractRenderState}
 * just before this render() call.
 * <p>
 * Replaces the empty {@code LivingEntityRendererMixin} stub that was left as a
 * 1.21.11 port TODO when the render method's signature changed from taking the
 * entity directly to taking a {@link LivingEntityRenderState}.
 * <p>
 * Targeting {@link LivingEntityRenderer} (not just AvatarRenderer) is intentional
 * — the rotation is applied via a thread-local that's only populated for player
 * casters by {@code AvatarRendererMixin}, so non-player living entities consume
 * a zero angle and no-op. Doing it here means the rotation wraps the entire
 * player render (model + held items + name tag), matching the source mod's
 * Fabric implementation.
 */
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererSpinMixin {

    @Inject(method = "render(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("HEAD"))
    private void megamod$applySpellSpin(LivingEntityRenderState state, PoseStack poseStack,
                                        MultiBufferSource bufferSource, int packedLight,
                                        CallbackInfo ci) {
        float angle = SpellSpinContext.consume();
        if (angle == 0f) return;
        poseStack.mulPose(Axis.YP.rotationDegrees(angle));
    }
}
