package com.ultra.megamod.mixin;

import com.ultra.megamod.feature.combat.animation.client.ThirdPersonSwingAnimator;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Applies the manual third-person weapon-swing fallback animation to a player model.
 * <p>
 * Runs at the TAIL of {@code HumanoidModel.setupAnim} so it executes BEFORE
 * {@code PlayerModelMixin}'s {@code setupAnim} RETURN injector (priority 2001),
 * which means any active PlayerAnimationLib keyframe animation (attack stack,
 * spell-cast stack, etc.) will overwrite our fallback rotations a moment later.
 * Net effect: PAL animations win when present, our fallback fills the gap when
 * no PAL animation is running (or hasn't replicated yet from the network).
 * <p>
 * Entity identity is taken from {@link ThirdPersonSwingAnimator}'s own thread-local
 * (set by {@code AvatarRendererMixin.extractRenderState}). That thread-local is
 * cleared inside {@code applySwingIfActive}, so a subsequent {@code setupAnim}
 * call on a non-player humanoid model (zombie, villager, …) sees -1 and
 * harmlessly no-ops — i.e. zombies never inherit a nearby player's swing pose.
 */
@Mixin(HumanoidModel.class)
public class HumanoidModelSwingMixin {

    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;)V",
            at = @At("TAIL"))
    private void megamod$applyAnimations(HumanoidRenderState state, CallbackInfo ci) {
        ThirdPersonSwingAnimator.applySwingIfActive(this);
    }
}
