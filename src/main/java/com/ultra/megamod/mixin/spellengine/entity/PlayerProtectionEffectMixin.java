package com.ultra.megamod.mixin.spellengine.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import com.ultra.megamod.lib.spellengine.api.effect.Protection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public class PlayerProtectionEffectMixin {
    @Inject(method = "isInvulnerableTo", at = @At("HEAD"), cancellable = true)
    private void isInvulnerable_SpellEngine(ServerLevel serverLevel, DamageSource damageSource, CallbackInfoReturnable<Boolean> cir) {
        if (Protection.tryProtect((Player) (Object) this, damageSource)) {
            cir.setReturnValue(true);
            cir.cancel();
        }
    }
}
