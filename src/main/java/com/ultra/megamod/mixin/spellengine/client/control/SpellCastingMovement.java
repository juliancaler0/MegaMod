package com.ultra.megamod.mixin.spellengine.client.control;

import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Input;
import com.ultra.megamod.lib.spellengine.SpellEngineMod;
import com.ultra.megamod.lib.spellengine.internals.casting.SpellCasterClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public class SpellCastingMovement {
    @Shadow public ClientInput input;
    @Shadow protected int sprintTriggerTime;

    @Inject(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/ClientInput;tick()V", shift = At.Shift.AFTER))
    private void aiStep_ModifyInput(CallbackInfo ci) {
        var player = (LocalPlayer) (Object) this;
        var caster = (SpellCasterClient) player;
        var process = caster.getSpellCastProcess();
        float multiplier = 1.0f;
        if (process != null && process.spell().value().active.cast != null && !player.isPassenger()) {
            multiplier = process.spell().value().active.cast.movement_speed * SpellEngineMod.config.movement_multiplier_speed_while_casting;
        }
        var attack = caster.getCurrentSkillAttack();
        if (attack != null) {
            multiplier = Math.min(multiplier, attack.attack.movement_speed());
        }
        if (multiplier <= 0) {
            // Fully suppress movement
            var kp = input.keyPresses;
            input.keyPresses = new Input(false, false, false, false, kp.jump(), kp.shift(), false);
            sprintTriggerTime = 0;
        } else if (multiplier < 1.0f) {
            // Suppress sprinting to slow down
            var kp = input.keyPresses;
            input.keyPresses = new Input(kp.forward(), kp.backward(), kp.left(), kp.right(), kp.jump(), kp.shift(), false);
            sprintTriggerTime = 0;
        }
    }
}
