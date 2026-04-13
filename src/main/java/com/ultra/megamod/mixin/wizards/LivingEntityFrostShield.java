package com.ultra.megamod.mixin.wizards;

import com.ultra.megamod.feature.combat.spell.SpellEffects;
import com.ultra.megamod.feature.combat.wizards.effect.FrostShielded;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityFrostShield implements FrostShielded {
    private boolean hasFrostShield = false;

    @Inject(method = "isBlocking", at = @At("HEAD"), cancellable = true)
    private void isBlocking_HEAD_FrostShield(CallbackInfoReturnable<Boolean> cir) {
        if (hasFrostShield) {
            cir.setReturnValue(true);
            cir.cancel();
        }
    }

    /**
     * In 1.21.11 isDamageSourceBlocked was removed. Instead we inject into hurtServer
     * to block all non-bypass damage when frost shield is active.
     */
    @Inject(method = "hurtServer", at = @At("HEAD"), cancellable = true)
    private void hurtServer_HEAD_FrostShield(ServerLevel level, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        // Block all damage except bypass-invulnerability when frost shield is active
        if (hasFrostShield && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }

    @Inject(method = "baseTick", at = @At("TAIL"))
    private void baseTick_TAIL_FrostShield(CallbackInfo ci) {
        var entity = (LivingEntity) ((Object) this);
        hasFrostShield = entity.hasEffect(SpellEffects.FROST_SHIELD);
        if (hasFrostShield && entity.isOnFire()) {
            entity.clearFire();
        }
    }

    public boolean hasFrostShield() {
        return hasFrostShield;
    }
}
