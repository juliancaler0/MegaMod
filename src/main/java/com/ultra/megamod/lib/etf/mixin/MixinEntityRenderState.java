package com.ultra.megamod.lib.etf.mixin;

import com.ultra.megamod.lib.etf.features.state.ETFEntityRenderState;
import com.ultra.megamod.lib.etf.features.state.HoldsETFRenderState;
import com.ultra.megamod.lib.etf.utils.ETFEntity;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Attaches an {@link ETFEntityRenderState} to every vanilla {@link EntityRenderState}.
 * Populated in {@code MixinEntityRenderer.extractRenderState} (TAIL) and read from the
 * {@link com.ultra.megamod.lib.etf.features.ETFRenderContext} when the {@code RenderType}
 * factory substitution fires.
 * <p>
 * Ported 1:1 from upstream ETF (1.21.3+ branch).
 */
@Mixin(EntityRenderState.class)
public class MixinEntityRenderState implements HoldsETFRenderState {

    @Unique
    private ETFEntityRenderState etf$state = null;

    @Override
    public @NotNull ETFEntityRenderState etf$getState() {
        // Null can occur if a render path queries before extractRenderState has run;
        // falling back to a dummy isn't possible since there's no ETFEntity here, so
        // throw — matches upstream's `assert`.
        if (etf$state == null) {
            throw new IllegalStateException("ETFEntityRenderState accessed before initialization");
        }
        return etf$state;
    }

    @Override
    public void etf$initState(@NotNull ETFEntity entity) {
        etf$state = ETFEntityRenderState.forEntity(entity);
        etf$state.setVanillaState((EntityRenderState) (Object) this);
    }
}
