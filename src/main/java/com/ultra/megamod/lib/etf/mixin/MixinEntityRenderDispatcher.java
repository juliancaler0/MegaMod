package com.ultra.megamod.lib.etf.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.ultra.megamod.lib.etf.features.ETFRenderContext;
import com.ultra.megamod.lib.etf.features.state.HoldsETFRenderState;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Sets / clears the current-entity context around the dispatcher's {@code submit} call
 * so the ETF texture-swap injector sees the right entity for every render.
 * <p>
 * The TAIL on {@code submit} also runs {@link ETFRenderContext#reset()} which clears
 * the currently-held entity + texture-swap state — important so the next entity
 * processed in the same frame starts with a clean slate.
 * <p>
 * Ported 1:1 from upstream ETF (1.21.11 branch — {@code submit} method name).
 */
@Mixin(EntityRenderDispatcher.class)
public class MixinEntityRenderDispatcher {

    @Inject(method = "submit", at = @At(value = "HEAD"))
    private <S extends EntityRenderState> void etf$grabContext(CallbackInfo ci, @Local S state) {
        if (state instanceof HoldsETFRenderState holder) {
            ETFRenderContext.setCurrentEntity(holder.etf$getState());
        }
    }

    @Inject(method = "submit", at = @At(value = "RETURN"))
    private void etf$clearContext(CallbackInfo ci) {
        ETFRenderContext.reset();
    }
}
