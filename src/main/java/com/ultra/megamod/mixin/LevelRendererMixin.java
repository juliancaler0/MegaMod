package com.ultra.megamod.mixin;

import com.ultra.megamod.feature.adminmodules.AdminModuleState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin into LevelRenderer to implement:
 * - Xray: trigger chunk rebuild when toggled so block transparency takes effect.
 *
 * The actual xray transparency is achieved by the Fullbright gamma mixin
 * combined with the FogRenderer mixin. This mixin forces a chunk re-render
 * when xray mode changes so the visual update is immediate.
 *
 * Uses setSectionsDirty() instead of allChanged() to avoid rebuilding the
 * entire render state, which is more compatible with Lithium/Radium chunk
 * optimizations and causes less of a lag spike.
 */
@Mixin(value = LevelRenderer.class, priority = 1100)
public class LevelRendererMixin {

    @Unique
    private static boolean megamod$lastXrayState = false;

    /**
     * At the start of each render frame, check if xray was toggled.
     * If so, schedule a chunk rebuild on the next tick rather than during
     * the render call itself. This avoids interfering with Lithium/Radium's
     * render-time chunk optimizations.
     */
    @Inject(method = "renderLevel", at = @At("HEAD"))
    private void megamod$checkXrayToggle(CallbackInfo ci) {
        boolean current = AdminModuleState.xrayEnabled;
        if (current != megamod$lastXrayState) {
            megamod$lastXrayState = current;
            // Defer the rebuild to next tick so it doesn't run mid-render,
            // which is safer with Lithium/Radium chunk caching
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null) {
                mc.execute(() -> mc.levelRenderer.allChanged());
            }
        }
    }
}
