package com.ultra.megamod.lib.owo.mixin.ui;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.ultra.megamod.lib.owo.ui.base.BaseOwoContainerScreen;
import com.ultra.megamod.lib.owo.ui.base.BaseOwoScreen;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Screen.class)
public class ScreenMixin {

    @ModifyExpressionValue(method = "keyPressed", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;shouldCloseOnEsc()Z", ordinal = 0))
    private boolean dontCloseOwoScreens(boolean original) {
        //noinspection ConstantValue
        if ((Object) this instanceof BaseOwoScreen<?> || (Object) this instanceof BaseOwoContainerScreen<?, ?>) {
            return false;
        }

        return original;
    }
}
