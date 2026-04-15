package com.ultra.megamod.lib.emf.mixin.mixins.rendering;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.ultra.megamod.lib.emf.EMFManager;
import com.ultra.megamod.lib.emf.models.animation.EMFAnimationEntityContext;
import com.ultra.megamod.lib.emf.models.animation.state.EMFEntityRenderState;
import com.ultra.megamod.lib.emf.models.animation.state.EMFSubmitData;
import com.ultra.megamod.lib.etf.features.state.ETFEntityRenderState;
import com.ultra.megamod.lib.etf.features.state.HoldsETFRenderState;
import com.ultra.megamod.lib.etf.utils.ETFEntity;

@Mixin(BlockEntityRenderDispatcher.class)
public class MixinBlockEntityRenderDispatcher {

    @Inject(method = "submit", at = @At(value = "HEAD"))
    private static <T extends net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState> void emf$grabEntity2(CallbackInfo ci,
                   @Local(argsOnly = true) T blockEntity) {
        var state = (EMFEntityRenderState) ((HoldsETFRenderState) blockEntity).etf$getState();
        EMFAnimationEntityContext.setCurrentEntityIteration(state);
        EMFSubmitData.AWAITING_backupState = state;
    }

    @Inject(method = "submit", at = @At(value = "TAIL"))
    private static <T extends net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState> void emf$grabEntity2(CallbackInfo ci) {
        EMFSubmitData.AWAITING_backupState = null;
    }
}
