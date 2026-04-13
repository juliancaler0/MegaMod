package com.ultra.megamod.mixin.bettercombat.client;

import com.ultra.megamod.feature.combat.animation.api.MinecraftClient_BetterCombat;
import com.ultra.megamod.feature.combat.animation.client.misc.ItemStackViewerPlayer;
import com.ultra.megamod.feature.combat.animation.config.BetterCombatConfig;
import com.ultra.megamod.feature.combat.animation.utils.MathHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Client player mixin for movement speed reduction during attacks and ItemStack viewer tracking.
 * Ported 1:1 from BetterCombat (net.bettercombat.mixin.client.ClientPlayerEntityMixin).
 */
@Mixin(LocalPlayer.class)
public abstract class ClientPlayerEntityMixin implements ItemStackViewerPlayer {

    @Shadow
    protected abstract boolean isControlledCamera();

    @Inject(method = "aiStep", at = @At(value = "TAIL"))
    private void bettercombat$aiStep_ModifyInput(CallbackInfo ci) {
        var clientPlayer = (LocalPlayer) ((Object) this);
        if (!isControlledCamera()) {
            return;
        }
        float multiplier = (float) Math.min(Math.max(
                com.ultra.megamod.feature.combat.animation.config.ScopedCombatConfig.movementSpeedWhileAttacking(clientPlayer),
                0.0), 1.0);
        var client = (MinecraftClient_BetterCombat) Minecraft.getInstance();
        var attack = client.getCurrentAttack();
        if (attack != null) {
            // Attack-specific movement speed multiplier could be added here
        }
        if (multiplier == 1) {
            return;
        }
        if (clientPlayer.isPassenger() && !BetterCombatConfig.movement_speed_effected_while_mounting) {
            return;
        }

        var swingProgress = client.getSwingProgress();
        if (swingProgress < 0.98) {
            if (BetterCombatConfig.movement_speed_applied_smoothly) {
                double p2;
                if (swingProgress <= 0.5) {
                    p2 = MathHelper.easeOutCubic(swingProgress * 2);
                } else {
                    p2 = MathHelper.easeOutCubic(1 - ((swingProgress - 0.5) * 2));
                }
                multiplier = (float) (1.0 - (1.0 - multiplier) * p2);
            }
            clientPlayer.xxa *= multiplier;
            clientPlayer.zza *= multiplier;
        }
    }

    // MARK: ItemStackViewerPlayer

    @Unique
    private ItemStack bettercombat$viewedItemStack = null;

    @Override
    public void betterCombat_setViewedItemStack(@Nullable ItemStack itemStack) {
        bettercombat$viewedItemStack = itemStack;
    }

    @Override
    public ItemStack betterCombat_getViewedItemStack() {
        return bettercombat$viewedItemStack;
    }
}
