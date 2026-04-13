package com.ultra.megamod.lib.etf.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.ultra.megamod.lib.etf.features.ETFRenderContext;
import com.ultra.megamod.lib.etf.features.state.HoldsETFRenderState;
import com.ultra.megamod.lib.etf.utils.ETFEntity;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Core hook into the per-frame render-state extraction.
 * <p>
 * On {@code extractRenderState} TAIL we build the ETF render state from the live entity
 * and publish it as the current entity so the {@code RenderType} factory injector can
 * use it.
 * <p>
 * The {@code submit} HEAD/TAIL pair guards post-submit renderers (nametags, leashes)
 * against accidental texture substitution — they run AFTER the model draw so a swap
 * there would paint the wrong texture onto them.
 * <p>
 * Ported from upstream ETF (1.21.11 branch — {@code submit} method name, not {@code render}).
 */
@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer<T extends Entity, S extends EntityRenderState> {

    @Inject(method = "extractRenderState", at = @At(value = "TAIL"))
    private void etf$createRenderState(CallbackInfo ci, @Local(argsOnly = true) Entity entity, @Local(argsOnly = true) S state) {
        ((HoldsETFRenderState) state).etf$initState((ETFEntity) entity);
        ETFRenderContext.setCurrentEntity(((HoldsETFRenderState) state).etf$getState());
    }

    @Inject(method = "submit", at = @At(value = "HEAD"))
    private void etf$protectPostRenderersLikeNametag(CallbackInfo ci) {
        ETFRenderContext.preventRenderLayerTextureModify();
    }

    @Inject(method = "submit", at = @At(value = "TAIL"))
    private void etf$revertForRenderersThatCallSuperFirst(CallbackInfo ci) {
        ETFRenderContext.allowRenderLayerTextureModify();
    }
}
