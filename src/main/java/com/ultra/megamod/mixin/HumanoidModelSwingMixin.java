package com.ultra.megamod.mixin;

import com.ultra.megamod.feature.backpacks.client.BackpackRenderContext;
import com.ultra.megamod.feature.combat.animation.client.ThirdPersonSwingAnimator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Applies weapon swing animations to player models.
 * <p>
 * PlayerAnimationLib's own AvatarRendererMixin handles keyframe animation application
 * (spell cast, dodge, attack, etc.), so this mixin only needs to handle the
 * fallback weapon swing animation when no keyframe animation is active.
 */
@Mixin(HumanoidModel.class)
public class HumanoidModelSwingMixin {

    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;)V",
            at = @At("TAIL"))
    private void megamod$applyAnimations(HumanoidRenderState state, CallbackInfo ci) {
        int entityId = BackpackRenderContext.getEntityId();
        if (entityId < 0) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        Entity entity = mc.level.getEntity(entityId);
        if (!(entity instanceof AbstractClientPlayer player)) return;

        // PAL's AvatarRendererMixin handles keyframe animations automatically.
        // We only apply the weapon swing animation as a fallback for non-keyframe swings.
        ThirdPersonSwingAnimator.applySwingIfActive(this);
    }
}
