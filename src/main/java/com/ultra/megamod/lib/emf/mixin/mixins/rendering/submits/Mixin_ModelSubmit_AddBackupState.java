package com.ultra.megamod.lib.emf.mixin.mixins.rendering.submits;

import net.minecraft.client.model.Model;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.client.renderer.SubmitNodeStorage;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.ultra.megamod.lib.emf.EMFManager;
import com.ultra.megamod.lib.emf.models.animation.EMFAnimationEntityContext;
import com.ultra.megamod.lib.emf.models.animation.state.EMFEntityRenderState;
import com.ultra.megamod.lib.emf.models.animation.state.EMFSubmitData;
import com.ultra.megamod.lib.emf.models.parts.EMFModelPartRoot;
import com.ultra.megamod.lib.emf.models.animation.state.EMFSubmitExtension;

@Mixin(SubmitNodeStorage.ModelSubmit.class)
public abstract class Mixin_ModelSubmit_AddBackupState<S> implements EMFSubmitExtension {

    @Unique private final EMFSubmitData data = new EMFSubmitData();

    @Override
    public EMFSubmitData emf$getData() {
        return data;
    }

    @Shadow
    public abstract Model<? super S> model();

    @Inject(method = "<init>", at = @At("TAIL"))
    private void emf$init(CallbackInfo ci) {
        if (EMFSubmitData.AWAITING_backupState != null) {
            // this is for those dumb block entities that don't pass the state through because they only need 1 primitive of state data
            data.backupState = EMFSubmitData.AWAITING_backupState;
        }

        if (EMFSubmitData.AWAITING_bipedPose != null) {
            data.bipedPose = EMFSubmitData.AWAITING_bipedPose;
        }

        data.onShoulder = EMFAnimationEntityContext.isOnShoulder();

        EMFModelPartRoot emfRoot = model().root() instanceof EMFModelPartRoot ? (EMFModelPartRoot) model().root() : null;
        if (emfRoot != null) {
            data.modelVariant = emfRoot.currentModelVariant;
        }
    }

}
