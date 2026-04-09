package com.ultra.megamod.mixin;

import com.ultra.megamod.feature.adminmodules.AdminModuleState;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin into Gui to implement:
 * - AntiOverlay: suppress rendering of pumpkin blur, water overlay, and fire overlay.
 *
 * In 1.21.11, overlay rendering is handled through the GUI layer system.
 * The methods renderCameraOverlays or the individual overlay methods can be intercepted.
 */
@Mixin(Gui.class)
public class GuiMixin {

    /**
     * AntiOverlay: Cancel the rendering of the carved pumpkin blur overlay.
     * Method signature in 1.21.11: renderItemHotbar with pumpkin check,
     * but the actual overlay is rendered in a separate method.
     *
     * We target renderCameraOverlays which handles pumpkin, powder snow, and
     * other full-screen texture overlays.
     */
    @Inject(method = "renderCameraOverlays", at = @At("HEAD"), cancellable = true)
    private void megamod$cancelCameraOverlays(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (AdminModuleState.antiOverlayEnabled) {
            ci.cancel();
        }
    }
}
