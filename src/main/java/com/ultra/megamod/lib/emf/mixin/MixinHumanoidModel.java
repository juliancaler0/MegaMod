package com.ultra.megamod.lib.emf.mixin;

import com.ultra.megamod.lib.emf.runtime.EmfModelBinder;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Drives the EMF render pipeline for humanoid entities (zombies, skeletons,
 * villagers, players, piglins, etc. — the vast majority of {@code HumanoidModel}
 * subclasses use the base {@code setupAnim} signature).
 * <p>
 * TAIL injection: after vanilla has written its default pose, bind the active
 * EMF model for the current entity (via {@link EmfModelBinder}) and apply the
 * compiled expressions on top. This matches upstream EMF's approach of running
 * pack animations AFTER vanilla so the pack can add to / override specific bones
 * without reimplementing every vanilla calculation from scratch.
 * <p>
 * {@code require = 0} so that a future MC change to {@code HumanoidModel}
 * doesn't hard-fail this mixin; ETF's texture-swap still covers the entity in
 * that case.
 */
@Mixin(HumanoidModel.class)
public abstract class MixinHumanoidModel {

    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;)V",
            at = @At("TAIL"),
            require = 0)
    private void emf$applyCompiledAnimations(HumanoidRenderState state, CallbackInfo ci) {
        HumanoidModel<?> self = (HumanoidModel<?>) (Object) this;
        // Walk to the root ModelPart: HumanoidModel exposes `body` / `head` etc.
        // but not a direct `root` accessor on 1.21.11. We use the base EntityModel
        // root via its public root() accessor.
        EmfModelBinder.applyForCurrent(self, self.root());
    }
}
