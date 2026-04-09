package com.ultra.megamod.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Lowers the shield when blocking so it doesn't take up as much screen space.
 * Translates the entire first-person hand render downward while the player is blocking.
 */
@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {

    @Inject(method = "renderHandsWithItems", at = @At("HEAD"))
    private void megamod$lowerShieldWhenBlocking(float partialTick, PoseStack poseStack,
            SubmitNodeCollector collector, LocalPlayer player,
            int packedLight, CallbackInfo ci) {
        if (player != null && player.isBlocking()) {
            poseStack.translate(0.0, -0.25, 0.0);
        }
    }
}
