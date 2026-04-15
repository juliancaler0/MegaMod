package com.ultra.megamod.lib.etf.mixin.mixins.entity.misc;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.ultra.megamod.lib.etf.features.ETFRenderContext;
import com.ultra.megamod.lib.etf.features.state.ETFEntityRenderState;
import com.ultra.megamod.lib.etf.features.state.HoldsETFRenderState;
import com.ultra.megamod.lib.etf.utils.ETFEntity;

@Mixin(EntityRenderDispatcher.class)
public class MixinEntityRenderDispatcher {
        @Inject(method = "submit", at = @At(value = "HEAD"))
    private <S extends net.minecraft.client.renderer.entity.state.EntityRenderState>
        void etf$grabContext(final CallbackInfo ci, @SuppressWarnings("LocalMayBeArgsOnly") @Local S state) {
        ETFRenderContext.setCurrentEntity(((HoldsETFRenderState) state).etf$getState());
    }

    @Inject(method =
            "submit",
            at = @At(value = "RETURN"))
    private void etf$clearContext(CallbackInfo ci) {
        ETFRenderContext.reset();
    }

}
