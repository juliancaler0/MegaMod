package com.ultra.megamod.mixin.rogues;

import com.ultra.megamod.feature.combat.rogues.RoguesMod;
import com.ultra.megamod.feature.combat.spell.SpellEffects;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin that reduces mob follow range when the target is in stealth.
 * Ported from net.rogues.mixin.TrackTargetGoalStealth.
 *
 * When a mob's target has the STEALTH or SHADOW_STEP effect, the follow
 * range is reduced to the configured stealth_follow_range (default 1.0 block),
 * effectively making mobs unable to track stealthed players at range.
 */
@Mixin(TargetGoal.class)
public class TrackTargetGoalStealth {
    @Shadow @Final protected Mob mob;

    @Inject(method = "getFollowDistance", at = @At("HEAD"), cancellable = true)
    private void getFollowRange_HEAD(CallbackInfoReturnable<Double> cir) {
        var target = mob.getTarget();
        if (target != null
                && (target.hasEffect(SpellEffects.STEALTH) || target.hasEffect(SpellEffects.SHADOW_STEP_BUFF))) {
            cir.setReturnValue(RoguesMod.tweaksConfig.stealth_follow_range);
            cir.cancel();
        }
    }
}
