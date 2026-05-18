package com.ultra.megamod.mixin;

import com.ultra.megamod.feature.backpacks.client.BackpackRenderContext;
import com.ultra.megamod.feature.combat.animation.client.SpellSpinContext;
import com.ultra.megamod.feature.combat.animation.client.ThirdPersonSwingAnimator;
import com.ultra.megamod.lib.playeranim.minecraft.api.PlayerAnimationAccess;
import com.ultra.megamod.lib.spellengine.internals.casting.SpellCasterEntity;
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

        // Whirlwind-style body spin: when the player is mid-cast on a spell whose
        // active.cast.animation_spin is non-zero, rotate the entire model around
        // its vertical axis. Replaces the empty {@code LivingEntityRendererMixin}
        // stub that was left as a 1.21.11 port TODO. The actual rotation is
        // applied by {@code LivingEntityRendererSpinMixin.render} HEAD by
        // consuming the angle from {@link SpellSpinContext}.
        if (player instanceof SpellCasterEntity caster) {
            var process = caster.getSpellCastProcess();
            if (process != null) {
                var spell = process.spell().value();
                if (spell != null && spell.active != null && spell.active.cast != null) {
                    float spin = spell.active.cast.animation_spin;
                    if (spin != 0f) {
                        long castTicks = player.level().getGameTime() - process.startedAt();
                        // Smooth between game ticks using partialTick so the spin
                        // doesn't appear to step at 20 Hz.
                        float angle = ((float) castTicks + partialTick) * spin;
                        SpellSpinContext.set(angle);
                    }
                }
            }
        }
    }
}
