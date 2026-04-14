package com.ultra.megamod.lib.etf.mixin;

import com.ultra.megamod.lib.etf.features.ETFRenderContext;
import com.ultra.megamod.lib.etf.features.state.ETFEntityRenderState;
import com.ultra.megamod.lib.etf.features.state.HoldsETFRenderState;
import com.ultra.megamod.lib.etf.utils.ETFEntity;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Block-entity twin of {@link MixinEntityRenderState}: attaches an ETF render state and
 * populates it in {@code extractBase} so the texture-swap hook can find the block
 * entity's identity when its renderer asks for a {@code RenderType}.
 * <p>
 * Ported 1:1 from upstream ETF (1.21.11 branch).
 */
@Mixin(BlockEntityRenderState.class)
public class MixinBlockEntityRenderState implements HoldsETFRenderState {

    @Unique
    private ETFEntityRenderState etf$state = null;

    @Override
    public @NotNull ETFEntityRenderState etf$getState() {
        if (etf$state == null) {
            throw new IllegalStateException("ETFEntityRenderState accessed before initialization");
        }
        return etf$state;
    }

    @Override
    public void etf$initState(@NotNull ETFEntity entity) {
        etf$state = ETFEntityRenderState.forEntity(entity);
        etf$state.setVanillaBlockState((BlockEntityRenderState) (Object) this);
    }

    @Inject(method = "extractBase", at = @At(value = "HEAD"))
    private static void etf$createRenderState(BlockEntity blockEntity, BlockEntityRenderState state, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay, CallbackInfo ci) {
        HoldsETFRenderState holder = (HoldsETFRenderState) state;
        holder.etf$initState((ETFEntity) blockEntity);
        ETFRenderContext.setCurrentEntity(holder.etf$getState());
    }
}
