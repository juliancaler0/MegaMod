package com.ultra.megamod.feature.hud;

import com.ultra.megamod.MegaMod;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;

/**
 * Applies camera shake when receiving heavy damage.
 * Triggered by ScreenShakePayload from server.
 */
@EventBusSubscriber(modid = MegaMod.MODID, value = Dist.CLIENT)
public class ScreenShakeEffect {

    private static volatile float shakeIntensity = 0;
    private static volatile long shakeStartMs = 0;
    private static final long SHAKE_DURATION_MS = 300;
    private static final java.util.Random RAND = new java.util.Random();

    public static void trigger(float intensity) {
        shakeIntensity = Math.min(1.0f, intensity);
        shakeStartMs = System.currentTimeMillis();
    }

    @SubscribeEvent
    public static void onCameraSetup(ViewportEvent.ComputeCameraAngles event) {
        if (shakeIntensity <= 0) return;

        long elapsed = System.currentTimeMillis() - shakeStartMs;
        if (elapsed > SHAKE_DURATION_MS) {
            shakeIntensity = 0;
            return;
        }

        // Linear decay
        float decay = 1.0f - (float) elapsed / SHAKE_DURATION_MS;
        float amplitude = shakeIntensity * decay * 2.5f;

        // Apply random yaw/pitch offsets
        float yawOffset = (RAND.nextFloat() - 0.5f) * amplitude;
        float pitchOffset = (RAND.nextFloat() - 0.5f) * amplitude;

        event.setYaw(event.getYaw() + yawOffset);
        event.setPitch(event.getPitch() + pitchOffset);
    }
}
