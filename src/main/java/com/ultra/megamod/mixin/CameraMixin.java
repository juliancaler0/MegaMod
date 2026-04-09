package com.ultra.megamod.mixin;

import com.ultra.megamod.feature.adminmodules.AdminModuleState;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin into Camera to implement:
 * - CameraClip: prevent camera from clipping into blocks in third-person view
 *   by making getMaxZoom return the full requested distance without reduction.
 *
 * In vanilla, Camera.getMaxZoom(float) performs a raycast from the player
 * to the camera position and returns a shorter distance if blocks are in the way.
 * When CameraClip is enabled, we override it to always return the full distance.
 */
@Mixin(Camera.class)
public class CameraMixin {

    /**
     * CameraClip: Override the max zoom distance to prevent camera collision.
     * The vanilla method clips the camera when blocks are between the player
     * and camera. We return the full distance so the camera passes through blocks.
     */
    @Inject(method = "getMaxZoom", at = @At("HEAD"), cancellable = true)
    private void megamod$noCameraClip(float desiredDistance, CallbackInfoReturnable<Float> cir) {
        if (AdminModuleState.cameraClipEnabled) {
            cir.setReturnValue(desiredDistance);
        }
    }
}
