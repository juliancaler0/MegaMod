package com.ultra.megamod.mixin;

import com.ultra.megamod.feature.backpacks.client.BackpackRenderContext;
import com.ultra.megamod.feature.combat.animation.client.ThirdPersonSwingAnimator;
import com.ultra.megamod.lib.playeranim.minecraft.api.PlayerAnimationAccess;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin into AvatarRenderer to capture the entity ID of the player being rendered.
 *
 * In 1.21.11, the rendering pipeline splits entity state extraction (extractRenderState)
 * from actual rendering (submit). By the time render layers run, the LivingEntityRenderState
 * does not carry entity identity. This mixin captures the player's entity ID during
 * extractRenderState and stores it in a thread-local (BackpackRenderContext) so that
 * BackpackLayerRenderer can determine which player it is rendering for.
 *
 * This enables backpack rendering on ALL players, not just the local player.
 */
@Mixin(AvatarRenderer.class)
public class AvatarRendererMixin {

    /**
     * Capture the entity ID of the player being rendered.
     * extractRenderState is called once per player per frame, before any layers render.
     *
     * The generic type AvatarlikeEntity erases to Avatar in bytecode, so
     * the method descriptor uses Avatar as the first parameter type.
     */
    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V",
            at = @At("HEAD"))
    private void megamod$captureEntityId(Avatar player, AvatarRenderState renderState, float partialTick, CallbackInfo ci) {
        BackpackRenderContext.setEntityId(player.getId());
        ThirdPersonSwingAnimator.setRenderingEntityId(player.getId());
        ThirdPersonSwingAnimator.setRenderingEntityUUID(player.getUUID());

        // NOTE: PlayerAnimator tick moved to SwingParticleRenderer.onClientTick
        // to run once per game tick (20/s), NOT per render frame (60fps).
        // Ticking per render frame made animations run 3x too fast.
    }
}
