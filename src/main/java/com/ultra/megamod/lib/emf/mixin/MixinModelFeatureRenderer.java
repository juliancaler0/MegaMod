package com.ultra.megamod.lib.emf.mixin;

import com.ultra.megamod.lib.emf.runtime.EmfModelBinder;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.model.EntityModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;

/**
 * Central hook into the per-submit render loop.
 * <p>
 * {@code ModelFeatureRenderer.renderModel} is what the vanilla renderer uses to
 * (a) call {@code model.setupAnim(state)} for the entity and (b) submit the
 * drawn triangles. We inject TAIL-of-setupAnim so pack transforms land after
 * every vanilla subclass has written its default pose — regardless of whether
 * it's a humanoid, quadruped, or a boss with a custom model class.
 * <p>
 * This is the "catch-all" that upstream EMF uses ({@code Mixin_ModelRenderer}).
 * It replaces the per-model-class mixins you'd otherwise need (armadillo, wolf,
 * dragon, etc.): if a subclass overrides {@code setupAnim} with a specialised
 * body, the vtable dispatch still routes through this invoke site.
 * <p>
 * {@code require = 0} so a future mapping change to {@code renderModel} doesn't
 * hard-fail the mixin; {@link MixinHumanoidModel} is the independent fallback
 * path for the humanoid tree.
 */
@Mixin(ModelFeatureRenderer.class)
public abstract class MixinModelFeatureRenderer {

    @Inject(method = "renderModel",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/model/Model;setupAnim(Ljava/lang/Object;)V",
                    shift = At.Shift.AFTER),
            require = 0)
    private <S> void emf$applyAfterSetupAnim(CallbackInfo ci,
                                              @Local(argsOnly = true) SubmitNodeStorage.ModelSubmit<S> modelSubmit) {
        Model<?> model = modelSubmit.model();
        if (!(model instanceof EntityModel<?> entityModel)) return;
        EmfModelBinder.applyForCurrent(entityModel, entityModel.root());
    }
}
