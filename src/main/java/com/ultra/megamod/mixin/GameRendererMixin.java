package com.ultra.megamod.mixin;

import com.ultra.megamod.feature.adminmodules.AdminModuleState;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin into GameRenderer to implement:
 * - NoBob: cancel view bobbing entirely
 *
 * Note: FOV modification (Zoom, CustomFOV) is handled via NeoForge's
 * ViewportEvent.ComputeFov in AdminModuleClientHooks instead of a mixin.
 */
@Mixin(value = GameRenderer.class, priority = 1100)
public class GameRendererMixin {

    /**
     * NoBob: Cancel the view bobbing method entirely when enabled.
     * In 1.21.11, the method is called bobView and applies walking/damage bob.
     */
    @Inject(method = "bobView", at = @At("HEAD"), cancellable = true)
    private void megamod$cancelBobView(CallbackInfo ci) {
        if (AdminModuleState.noBobEnabled) {
            ci.cancel();
        }
    }
}
