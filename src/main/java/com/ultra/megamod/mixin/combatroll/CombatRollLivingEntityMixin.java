package com.ultra.megamod.mixin.combatroll;

import com.ultra.megamod.lib.combatroll.api.RollInvulnerable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class CombatRollLivingEntityMixin implements RollInvulnerable {
    @Unique
    private int combatroll$invulnerableTicks = 0;

    @Inject(method = "tick", at = @At("TAIL"))
    private void combatroll$tick_TAIL(CallbackInfo ci) {
        if (combatroll$invulnerableTicks > 0) {
            combatroll$invulnerableTicks -= 1;
        }
    }

    @Inject(method = "hurtServer", at = @At("HEAD"), cancellable = true)
    private void combatroll$hurtServer_HEAD(ServerLevel world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (combatroll$invulnerableTicks > 0) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }

    @Override
    public void setRollInvulnerableTicks(int ticks) {
        combatroll$invulnerableTicks = ticks;
    }
}
