package com.ultra.megamod.mixin.accessories.client;

import com.ultra.megamod.lib.accessories.client.AccessoriesClient;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Inject(method = "resizeDisplay", at = @At(value = "TAIL"))
    private void captureResize(CallbackInfo ci){
        var client = ((Minecraft) ((Object) this));
        AccessoriesClient.WINDOW_RESIZE_CALLBACK_EVENT.invoker().onResized(client, client.getWindow());
    }
}
