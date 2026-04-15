package com.ultra.megamod.lib.etf.mixin.mixins.entity.misc;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.ultra.megamod.lib.etf.ETF;
import com.ultra.megamod.lib.etf.features.ETFRenderContext;
import com.ultra.megamod.lib.etf.features.state.ETFEntityRenderState;
import com.ultra.megamod.lib.etf.features.state.HoldsETFRenderState;
import com.ultra.megamod.lib.etf.utils.ETFEntity;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;

@Mixin(BlockEntityRenderDispatcher.class)
public class MixinBlockEntityRenderDispatcher {

    private static final String RENDER_METHOD =
            "submit";

    @Inject(method = RENDER_METHOD, at = @At(value = "HEAD"))
    private static <S extends BlockEntityRenderState> void etf$grabContext(final CallbackInfo ci, @Local(argsOnly = true) S state) {

        ETFRenderContext.setCurrentEntity(((HoldsETFRenderState) state).etf$getState());

    }

    @Inject(method = RENDER_METHOD, at = @At(value = "RETURN"))
    private static void etf$clearContext(CallbackInfo ci) {
        ETFRenderContext.reset();
    }


}
