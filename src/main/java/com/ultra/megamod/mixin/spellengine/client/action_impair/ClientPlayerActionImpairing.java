package com.ultra.megamod.mixin.spellengine.client.action_impair;

import net.minecraft.client.player.LocalPlayer;
import com.ultra.megamod.lib.spellengine.api.effect.EntityActionsAllowed;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public class ClientPlayerActionImpairing {
    @Inject(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/ClientInput;tick()V", shift = At.Shift.AFTER))
    private void tickMovement_ModifyInput_SpellEngine_ActionImpairing(CallbackInfo ci) {
        var clientPlayer = (LocalPlayer)((Object)this);
        if (EntityActionsAllowed.isImpaired(clientPlayer, EntityActionsAllowed.Common.MOVE)) {
            var input = clientPlayer.input.keyPresses;
            clientPlayer.input.keyPresses = new net.minecraft.world.entity.player.Input(
                    false, false, false, false,
                    input.jump(), input.shift(), input.sprint()
            );
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void tick_TAIL_SpellEngine_ActionImpairing(CallbackInfo ci) {
        ((EntityActionsAllowed.ControlledEntity)this).updateEntityActionsAllowed();
    }
}
