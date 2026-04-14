package com.ultra.megamod.lib.etf.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.ultra.megamod.lib.etf.features.ETFRenderContext;
import com.ultra.megamod.lib.etf.features.state.HoldsETFRenderState;
import net.minecraft.client.renderer.entity.IllusionerRenderer;
import net.minecraft.client.renderer.entity.state.IllusionerRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Illusioner renders multiple copies of itself per frame via a {@code super.submit(...)}
 * loop, which doesn't route through {@link net.minecraft.client.renderer.entity.EntityRenderDispatcher}.
 * Without this hook the first illusion copy would get the variant texture and the
 * subsequent copies would miss the current-entity context. Each loop iteration we
 * re-assert the entity + flip the render-layer-modify flag back on.
 * <p>
 * Ported 1:1 from upstream ETF (1.21.11 branch).
 */
@Mixin(IllusionerRenderer.class)
public abstract class MixinIllusionerRenderer {

    @Unique
    private HoldsETFRenderState etf$heldState = null;

    @Inject(method = "submit", at = @At(value = "HEAD"), require = 0)
    private void etf$start(CallbackInfo ci, @Local(argsOnly = true) IllusionerRenderState state) {
        etf$heldState = (HoldsETFRenderState) state;
        ETFRenderContext.setCurrentEntity(etf$heldState.etf$getState());
    }

    @Inject(method = "submit",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/entity/IllagerRenderer;submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V"),
            require = 0)
    private void etf$loop(CallbackInfo ci) {
        // re-assert main entity before each illusion copy render
        if (etf$heldState != null) {
            ETFRenderContext.setCurrentEntity(etf$heldState.etf$getState());
        }
        ETFRenderContext.allowRenderLayerTextureModify();
        ETFRenderContext.endSpecialRenderOverlayPhase();
    }
}
