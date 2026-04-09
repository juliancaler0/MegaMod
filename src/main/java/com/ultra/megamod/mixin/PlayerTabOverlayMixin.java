package com.ultra.megamod.mixin;

import com.ultra.megamod.feature.adminmodules.AdminModuleState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.ChatFormatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin into PlayerTabOverlay to implement:
 * - BetterTab: enhance the player tab list with health info and ping colors.
 *
 * We modify the display name returned for each player to include health
 * and a color-coded ping indicator.
 */
@Mixin(PlayerTabOverlay.class)
public class PlayerTabOverlayMixin {

    /**
     * BetterTab: Modify the displayed name in the tab list to include
     * health and ping information for each player.
     */
    @Inject(method = "getNameForDisplay", at = @At("RETURN"), cancellable = true)
    private void megamod$enhanceTabDisplay(PlayerInfo playerInfo, CallbackInfoReturnable<Component> cir) {
        if (!AdminModuleState.betterTabEnabled) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        Component original = cir.getReturnValue();
        MutableComponent enhanced = Component.empty();

        // Ping color indicator
        int latency = playerInfo.getLatency();
        ChatFormatting pingColor;
        if (latency < 50) pingColor = ChatFormatting.GREEN;
        else if (latency < 100) pingColor = ChatFormatting.YELLOW;
        else if (latency < 200) pingColor = ChatFormatting.GOLD;
        else pingColor = ChatFormatting.RED;

        enhanced.append(Component.literal("[" + latency + "ms] ").withStyle(pingColor));
        enhanced.append(original);

        // Try to find the player entity for health info using direct UUID lookup
        if (mc.level != null && playerInfo.getProfile() != null) {
            Player player = mc.level.getPlayerByUUID(playerInfo.getProfile().id());
            if (player != null) {
                float health = player.getHealth();
                float maxHealth = player.getMaxHealth();
                ChatFormatting healthColor;
                if (health > maxHealth * 0.66f) healthColor = ChatFormatting.GREEN;
                else if (health > maxHealth * 0.33f) healthColor = ChatFormatting.YELLOW;
                else healthColor = ChatFormatting.RED;

                enhanced.append(Component.literal(" " + String.format("%.0f", health) + "HP")
                        .withStyle(healthColor));
            }
        }

        cir.setReturnValue(enhanced);
    }
}
