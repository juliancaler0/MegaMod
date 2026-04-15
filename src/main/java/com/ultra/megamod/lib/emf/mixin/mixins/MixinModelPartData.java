package com.ultra.megamod.lib.emf.mixin.mixins;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.PartDefinition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.ultra.megamod.lib.emf.EMF;
import com.ultra.megamod.lib.emf.utils.IEMFTextureSizeSupplier;

@Mixin(PartDefinition.class)
public class MixinModelPartData {
    @Inject(method = "bake",
            at = @At(value = "RETURN"))
    private void emf$injectAnnouncerData(final int textureWidth, final int textureHeight, final CallbackInfoReturnable<ModelPart> cir) {
        if (EMF.config().getConfig().modelExportMode.doesLog() && cir.getReturnValue() instanceof IEMFTextureSizeSupplier) {
            ((IEMFTextureSizeSupplier) cir.getReturnValue()).emf$setTextureSize(new int[]{textureWidth, textureHeight});
        }

    }
}