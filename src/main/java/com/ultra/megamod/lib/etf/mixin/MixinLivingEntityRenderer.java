package com.ultra.megamod.lib.etf.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.ultra.megamod.lib.etf.features.ETFRenderContext;
import com.ultra.megamod.lib.etf.features.state.ETFEntityRenderState;
import com.ultra.megamod.lib.etf.features.state.HoldsETFRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Feature-renderer bracketing for {@link LivingEntityRenderer}.
 * <p>
 * Marks the start/end of the feature-renderer loop so the variator knows whether the
 * base model or a feature-pass is currently drawing. Feature renderers can recurse into
 * other entities, so the current entity must be reasserted each loop iteration.
 * <p>
 * Hook points:
 * <ul>
 *   <li>{@code List.iterator()} — start of the layers loop; captures the state into
 *       a {@code @Share} local for reassertion.</li>
 *   <li>{@code Iterator.next()} — each loop iteration, re-push the entity as current.</li>
 *   <li>{@code PoseStack.popPose()} — end of feature rendering; clears the flag.</li>
 * </ul>
 * <p>
 * Ported from upstream ETF (1.21.11 branch — {@code submit} method name, not {@code render}).
 * {@code require = 0} on each injection so mixin doesn't hard-fail if a future MC change
 * renames one of the hooked symbols — the other two still bracket feature rendering.
 */
@Mixin(LivingEntityRenderer.class)
public abstract class MixinLivingEntityRenderer<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>>
        extends EntityRenderer<T, S> {

    @SuppressWarnings("unused")
    protected MixinLivingEntityRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Inject(method = "submit",
            at = @At(value = "INVOKE", target = "Ljava/util/List;iterator()Ljava/util/Iterator;"),
            require = 0)
    private void etf$markFeaturesStart(CallbackInfo ci,
                                       @Share("etfHeld") LocalRef<ETFEntityRenderState> held,
                                       @Local(argsOnly = true) S state) {
        held.set(((HoldsETFRenderState) state).etf$getState());
        ETFRenderContext.allowRenderLayerTextureModify();
        ETFRenderContext.setRenderingFeatures(true);
    }

    @Inject(method = "submit",
            at = @At(value = "INVOKE", target = "Ljava/util/Iterator;next()Ljava/lang/Object;"),
            require = 0)
    private void etf$markFeaturesLoopIteration(CallbackInfo ci,
                                               @Share("etfHeld") LocalRef<ETFEntityRenderState> held) {
        ETFEntityRenderState state = held.get();
        if (state != null) {
            ETFRenderContext.setCurrentEntity(state);
        }
        ETFRenderContext.allowRenderLayerTextureModify();
        ETFRenderContext.endSpecialRenderOverlayPhase();
    }

    @Inject(method = "submit",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V"),
            require = 0)
    private void etf$markFeaturesEnd(CallbackInfo ci) {
        ETFRenderContext.setRenderingFeatures(false);
    }
}
