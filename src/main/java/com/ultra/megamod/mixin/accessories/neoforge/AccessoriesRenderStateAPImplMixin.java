package com.ultra.megamod.mixin.accessories.neoforge;

import com.ultra.megamod.mixin.accessories.neoforge.neoforge.BaseRenderStateAccessor;
import com.ultra.megamod.lib.accessories.pond.AccessoriesRenderStateAPImpl;
import net.minecraft.util.context.ContextKey;
import net.neoforged.neoforge.client.renderstate.BaseRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = AccessoriesRenderStateAPImpl.class, remap = false)
public interface AccessoriesRenderStateAPImplMixin {
    @Inject(method = "getRenderData", at = @At(value = "HEAD"))
    private <T> void getIntoNeoforgeAPI(ContextKey<T> key, CallbackInfoReturnable<T> cir) {
        if (this instanceof BaseRenderState baseState && ((BaseRenderStateAccessor) baseState).accessories$extensions().containsKey(key)) {
            cir.setReturnValue(baseState.getRenderData(key));
        }
    }

    @Inject(method = "clearExtraData", at = @At(value = "HEAD"))
    private <T> void clearExtraData(CallbackInfo ci) {
        if (this instanceof BaseRenderState baseState) baseState.resetRenderData();
    }

    @Inject(method = "hasRenderData", at = @At(value = "HEAD"))
    private void containsIntoNeoforgeAPI(ContextKey<?> key, CallbackInfoReturnable<Boolean> cir) {
        if (this instanceof BaseRenderState baseState && ((BaseRenderStateAccessor) baseState).accessories$extensions().containsKey(key)) {
            cir.setReturnValue(true);
        }
    }
}
