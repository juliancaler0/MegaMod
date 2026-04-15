package com.ultra.megamod.lib.etf.mixin.mixins.entity.renderer;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.ultra.megamod.lib.etf.ETF;
import com.ultra.megamod.lib.etf.features.ETFRenderContext;
import com.ultra.megamod.lib.etf.features.state.HoldsETFRenderState;
import com.ultra.megamod.lib.etf.utils.ETFEntity;

@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer<T extends Entity
        , S extends EntityRenderState> {

    @Inject(method = "extractRenderState",
    at = @At(value = "TAIL"))
    private void etf$createRenderState(final CallbackInfo ci, @Local(argsOnly = true) Entity entity, @Local(argsOnly = true) S state) {
        ((HoldsETFRenderState) state).etf$initState((ETFEntity) entity);
        ETFRenderContext.setCurrentEntity(((HoldsETFRenderState) state).etf$getState()); // either this or the one in the dispatcher is redundant but ill put both for now
    }

    @Inject(method = "getPackedLightCoords", at = @At(value = "RETURN"), cancellable = true)
    private void etf$vanillaLightOverrideCancel(T entity, float tickDelta, CallbackInfoReturnable<Integer> cir) {
        //if need to override vanilla brightness behaviour
        //change return with overridden light value still respecting higher block and sky lights
        cir.setReturnValue(ETF.config().getConfig().getLightOverride(
                entity,
                tickDelta,
                cir.getReturnValue()));
    }

    private static final String RENDER = "submit";

    @Inject(method = RENDER, at = @At(value = "HEAD"))
    private void etf$protectPostRenderersLikeNametag(final CallbackInfo ci) {
        ETFRenderContext.preventRenderLayerTextureModify();
    }

    @Inject(method = RENDER, at = @At(value = "TAIL"))
    private void etf$revertForRenderersThatCallSuperFirst(final CallbackInfo ci) {
        ETFRenderContext.allowRenderLayerTextureModify(); // see minecart rendering
    }

}