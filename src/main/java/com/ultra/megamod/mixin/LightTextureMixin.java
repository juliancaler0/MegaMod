package com.ultra.megamod.mixin;

import com.ultra.megamod.feature.adminmodules.AdminModuleState;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.world.level.dimension.DimensionType;
import net.neoforged.fml.ModList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin into LightTexture to implement:
 * - Fullbright Gamma: override the brightness calculation to always return
 *   maximum brightness, eliminating darkness without needing Night Vision.
 *
 * This provides a cleaner fullbright than the server-side Night Vision effect:
 * no pulsing at low duration, no potion particles, works instantly.
 *
 * Disabled automatically when Embeddium/Sodium is present, as they replace
 * the lighting pipeline entirely and this override would conflict.
 */
@Mixin(LightTexture.class)
public class LightTextureMixin {

    @Unique
    private static final boolean megamod$sodiumPresent = ModList.get().isLoaded("embeddium")
            || ModList.get().isLoaded("sodium")
            || ModList.get().isLoaded("rubidium");

    /**
     * Override getBrightness to return 1.0 (max) when fullbright gamma is active.
     * Vanilla computes brightness from dimension type and light level.
     * We short-circuit to full brightness.
     * Skipped when Embeddium/Sodium is loaded to avoid lighting pipeline conflicts.
     */
    @Inject(method = "getBrightness", at = @At("HEAD"), cancellable = true)
    private static void megamod$fullbrightGamma(DimensionType dimensionType, int lightLevel, CallbackInfoReturnable<Float> cir) {
        if (AdminModuleState.fullbrightGammaEnabled && !megamod$sodiumPresent) {
            cir.setReturnValue(1.0f);
        }
    }
}
