package com.ultra.megamod.mixin;

import com.ultra.megamod.feature.adminmodules.AdminModuleState;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin into MultiPlayerGameMode to implement:
 * - NoBreakDelay: remove the delay between breaking consecutive blocks
 * - InstantMine: force instant block destruction on the client side
 *
 * The server-side modules already boost break speed via BreakSpeed events,
 * but the client still enforces a destroyDelay between blocks. This mixin
 * removes that delay for a seamless mining experience.
 */
@Mixin(MultiPlayerGameMode.class)
public class MultiPlayerGameModeMixin {

    @Shadow private int destroyDelay;

    /**
     * NoBreakDelay / InstantMine: reset the destroy delay to 0 every time
     * continueDestroyBlock is called, so there is no pause between blocks.
     */
    @Inject(method = "continueDestroyBlock", at = @At("HEAD"))
    private void megamod$removeBreakDelay(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        if (AdminModuleState.noBreakDelayEnabled || AdminModuleState.instantMineEnabled) {
            this.destroyDelay = 0;
        }
    }
}
