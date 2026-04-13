package com.ultra.megamod.mixin.bettercombat.client;

import com.ultra.megamod.feature.combat.animation.config.BetterCombatConfig;
import com.ultra.megamod.feature.combat.animation.logic.PlayerAttackHelper;
import com.ultra.megamod.mixin.bettercombat.player.LivingEntityAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fixes attack cooldown when cancelling block breaking with a BetterCombat weapon.
 * Ported 1:1 from BetterCombat (net.bettercombat.mixin.client.ClientPlayerInteractionManagerMixin).
 */
@Mixin(MultiPlayerGameMode.class)
public class ClientPlayerInteractionManagerMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "stopDestroyBlock", at = @At("TAIL"), require = 0)
    public void bettercombat$stopDestroyBlock_FixAttackCD(CallbackInfo ci) {
        try {
            var player = minecraft.player;
            if (player == null) return;
            var cooldownLength = PlayerAttackHelper.getAttackCooldownTicksCapped(player);
            float typicalUpswing = 0.5F;
            int reducedCooldown = Math.round(cooldownLength * typicalUpswing * BetterCombatConfig.getUpswingMultiplier());
            ((LivingEntityAccessor) player).bettercombat$setAttackStrengthTicker(reducedCooldown);
        } catch (Exception ignored) {
            // Ignore exceptions from weapon cooldown access
        }
    }
}
