package com.ultra.megamod.lib.emf.mixin;

import com.ultra.megamod.lib.emf.runtime.EmfModelBinder;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Generic {@code EntityModel.setupAnim} hook.
 * <p>
 * Catches every entity model that doesn't override the base signature — ground
 * coverage for the long tail of non-humanoid mobs (foliaath, naga, frostmaw,
 * wolves, horses, etc.). For humanoids, {@link MixinHumanoidModel} already
 * applies the pack transforms after the more specific setupAnim; this mixin
 * handles the base-class path.
 * <p>
 * The injection fires at TAIL (after vanilla's default pose) and applies EMF
 * compiled animations on top. No effect if the pack ships no {@code .jem} for
 * the current entity — the binder returns {@code null} and the apply call is a
 * no-op.
 * <p>
 * {@code require = 0} because a subclass that overrides the whole method will
 * legitimately never hit this HEAD/TAIL pair.
 */
@Mixin(EntityModel.class)
public abstract class MixinEntityModelSetup {

    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;)V",
            at = @At("TAIL"),
            require = 0)
    private void emf$applyCompiledAnimations(EntityRenderState state, CallbackInfo ci) {
        EntityModel<?> self = (EntityModel<?>) (Object) this;
        EmfModelBinder.applyForCurrent(self, self.root());
    }
}
