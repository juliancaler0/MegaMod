package com.ultra.megamod.lib.emf.mixin.mixins;


import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.ultra.megamod.lib.emf.EMF;
import com.ultra.megamod.lib.emf.models.parts.EMFModelPartRoot;
import com.ultra.megamod.lib.emf.models.IEMFModel;
import com.ultra.megamod.lib.emf.models.animation.EMFAnimationEntityContext;
import com.ultra.megamod.lib.emf.EMFManager;

import java.util.function.Function;


@Mixin(Model.class)
public class MixinModel implements IEMFModel {
    @Unique
    private EMFModelPartRoot emf$thisEMFModelRoot = null;


    @Inject(method = "<init>",
            at = @At(value = "TAIL"))
    private void emf$discoverEMFModel(final ModelPart modelPart, final Function<?,?> function, final CallbackInfo ci) {
        if (EMF.testForForgeLoadingError()) return;
        if(modelPart instanceof EMFModelPartRoot root) {
            emf$thisEMFModelRoot = root;
        }
        EMFManager.lastCreatedRootModelPart = null;
    }


    @Override
    public boolean emf$isEMFModel() {
        return emf$thisEMFModelRoot != null;
    }

    @Override
    public EMFModelPartRoot emf$getEMFRootModel() {
        return emf$thisEMFModelRoot;
    }


    @Inject(method = "renderType",
            at = @At(value = "HEAD"))
    private void emf$discoverEMFModel(CallbackInfoReturnable<RenderType> cir) {
        EMFAnimationEntityContext.setLayerFactory(((Model) ((Object) this)).renderType);
    }
}
