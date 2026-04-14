package com.ultra.megamod.lib.emf.mixin;

import com.ultra.megamod.lib.emf.runtime.EmfModelBinder;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.Model;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Generic {@code Model.setupAnim} hook.
 * <p>
 * Catches every model that doesn't override the base signature with its own
 * typed setupAnim. {@code Model.setupAnim(S)} is the base method that every
 * {@link EntityModel} subclass overrides with a typed variant (e.g.
 * {@code WolfModel.setupAnim(WolfRenderState)}). The JVM then creates a bridge
 * {@code setupAnim(Object)} that dispatches to the typed override.
 * <p>
 * This mixin runs TAIL on the bridge method (effectively: after any concrete
 * subclass has applied its pose). Because {@link Model} also covers block-entity
 * models, we gate to {@code EntityModel} subclasses only via the instanceof check
 * below so we don't attempt to apply pack animations to chest / book models.
 * <p>
 * For {@link net.minecraft.client.model.HumanoidModel}, {@link MixinHumanoidModel}
 * provides an additional dedicated TAIL hook so humanoid-only pose tweaks (arm
 * pose states, swim amount interpolation) are in the bone map before EMF writes
 * its own values.
 * <p>
 * {@code require = 0} because the bridge method only exists at runtime after
 * type erasure; mixin may not always find it via the declared target, but
 * {@link MixinHumanoidModel} covers the humanoid path independently.
 */
@Mixin(Model.class)
public abstract class MixinEntityModelSetup {

    @Inject(method = "setupAnim(Ljava/lang/Object;)V",
            at = @At("TAIL"),
            require = 0)
    private void emf$applyCompiledAnimations(Object state, CallbackInfo ci) {
        Object self = this;
        if (!(self instanceof EntityModel<?> entityModel)) return;
        EmfModelBinder.applyForCurrent(entityModel, entityModel.root());
    }
}
