package com.ultra.megamod.lib.emf.mixin.mixins.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.ultra.megamod.lib.emf.models.animation.EMFAnimationEntityContext;

import net.minecraft.client.gui.render.GuiRenderer;
@Mixin(GuiRenderer.class)
public class Mixin_GuiEntityTester {
    @Inject(method = "render",
        at = @At("HEAD"))
    private void etf$beforeRenderToTexture(final CallbackInfo ci) {
        EMFAnimationEntityContext.setIsInGui = true;
    }

    @Inject(method = "render",
            at = @At("TAIL"))
    private void etf$afterRenderToTexture(final CallbackInfo ci) {
        EMFAnimationEntityContext.setIsInGui = false;
    }
}
