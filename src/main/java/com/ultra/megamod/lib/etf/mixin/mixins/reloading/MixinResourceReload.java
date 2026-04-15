package com.ultra.megamod.lib.etf.mixin.mixins.reloading;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.ultra.megamod.lib.etf.features.ETFManager;
import com.ultra.megamod.lib.etf.utils.ETFUtils2;

@Mixin(Minecraft.class)
public abstract class MixinResourceReload {


    @Inject(method = "onResourceLoadFinished", at = @At("HEAD"))
    private void etf$injected(CallbackInfo ci) {
        ETFUtils2.logMessage("reloading ETF data.");
        ETFManager.resetInstance();
    }
}

