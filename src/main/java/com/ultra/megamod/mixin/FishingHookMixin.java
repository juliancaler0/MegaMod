package com.ultra.megamod.mixin;

import com.ultra.megamod.feature.skills.capstone.CapstoneManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Reduce fishing bite time for players with Fisherman skill nodes.
 * T1: -10%, T2: -15%, T3: -20%, T4: -25%
 *
 * Applied once when timeUntilHooked transitions from 0 to a positive value
 * (i.e., when vanilla first sets the bite timer).
 */
@Mixin(FishingHook.class)
public class FishingHookMixin {

    @Shadow private int timeUntilHooked;

    @Unique private int megamod$lastTimeUntilHooked = 0;

    @Inject(method = "catchingFish", at = @At("TAIL"))
    private void megamod$reduceBiteTime(CallbackInfo ci) {
        // Detect when timeUntilHooked was just set from 0 to a positive value
        if (this.timeUntilHooked > 0 && this.megamod$lastTimeUntilHooked <= 0) {
            FishingHook hook = (FishingHook) (Object) this;
            Player owner = hook.getPlayerOwner();
            if (owner instanceof ServerPlayer player) {
                double reduction = 0.0;
                if (CapstoneManager.hasCapstoneTrigger(player, "fisherman_4")) {
                    reduction = 0.25;
                } else if (CapstoneManager.hasCapstoneTrigger(player, "fisherman_3")) {
                    reduction = 0.20;
                } else if (CapstoneManager.hasCapstoneTrigger(player, "fisherman_2")) {
                    reduction = 0.15;
                } else if (CapstoneManager.hasCapstoneTrigger(player, "fisherman_1")) {
                    reduction = 0.10;
                }

                if (reduction > 0.0) {
                    this.timeUntilHooked = Math.max(1, (int)(this.timeUntilHooked * (1.0 - reduction)));
                }
            }
        }
        this.megamod$lastTimeUntilHooked = this.timeUntilHooked;
    }
}
