package com.ultra.megamod.mixin.combatroll;

import com.ultra.megamod.lib.combatroll.CombatRollMod;
import com.ultra.megamod.lib.combatroll.client.animation.AnimatablePlayer;
import com.ultra.megamod.lib.combatroll.client.animation.RollAnimationController;
import com.ultra.megamod.lib.playeranim.minecraft.api.PlayerAnimationAccess;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AbstractClientPlayer.class)
public class CombatRollAbstractClientPlayerMixin implements AnimatablePlayer {

    @Override
    public void playRollAnimation(String animationName, Vec3 direction) {
        var controller = (RollAnimationController) PlayerAnimationAccess.getPlayerAnimationLayer(
            (AbstractClientPlayer)(Object)this,
            RollAnimationController.ID
        );

        if (controller != null) {
            controller.playRoll(animationName, direction, CombatRollMod.config.roll_duration);
        }
    }
}
