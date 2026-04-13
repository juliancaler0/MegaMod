package com.ultra.megamod.mixin.combatroll;

import com.ultra.megamod.lib.combatroll.CombatRollMod;
import com.ultra.megamod.lib.combatroll.internals.RollManager;
import com.ultra.megamod.lib.combatroll.internals.RollingEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.ClientInput;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocalPlayer.class)
public class CombatRollLocalPlayerMixin implements RollingEntity {
    @Unique
    private RollManager combatroll$rollManager = new RollManager();

    public RollManager getRollManager() {
        return combatroll$rollManager;
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void combatroll$tick_TAIL(CallbackInfo ci) {
        var player = (LocalPlayer) ((Object)this);
        if (player != null) {
            combatroll$rollManager.tick(player);
        }
    }

    @Shadow
    @Final
    protected Minecraft minecraft;

    @Inject(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/ClientInput;tick()V", shift = At.Shift.AFTER))
    private void combatroll$aiStep_ModifyInput(CallbackInfo ci) {
        var clientPlayer = (LocalPlayer) ((Object) this);
        var config = CombatRollMod.config;
        if (!config.allow_jump_while_rolling && combatroll$rollManager.isRolling()) {
            var input = clientPlayer.input.keyPresses;
            clientPlayer.input.keyPresses = new net.minecraft.world.entity.player.Input(
                    input.forward(),
                    input.backward(),
                    input.left(),
                    input.right(),
                    false,
                    input.shift(),
                    input.sprint()
            );
        }
    }

    @Inject(method = "isAutoJumpEnabled", at = @At("HEAD"), cancellable = true)
    public void combatroll$canStartAutoJump_HEAD(CallbackInfoReturnable<Boolean> cir) {
        var config = CombatRollMod.config;
        if (config != null) {
            if (combatroll$rollManager.isRolling() && !config.allow_auto_jump_while_rolling) {
                cir.setReturnValue(false);
                cir.cancel();
            }
        }
    }
}
