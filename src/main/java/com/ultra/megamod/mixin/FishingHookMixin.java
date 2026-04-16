package com.ultra.megamod.mixin;

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
 * TODO: Reconnect with Pufferfish Skills API (was CapstoneManager.hasCapstoneTrigger)
 * Currently no-op until new skill system is wired.
 */
@Mixin(FishingHook.class)
public class FishingHookMixin {

    @Shadow private int timeUntilHooked;

    @Unique private int megamod$lastTimeUntilHooked = 0;

    @Inject(method = "catchingFish", at = @At("TAIL"))
    private void megamod$reduceBiteTime(CallbackInfo ci) {
        // TODO: Reconnect with Pufferfish Skills API (was CapstoneManager-based reduction)
        this.megamod$lastTimeUntilHooked = this.timeUntilHooked;
    }
}
