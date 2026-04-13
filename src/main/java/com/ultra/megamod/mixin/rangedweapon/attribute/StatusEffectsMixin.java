package com.ultra.megamod.mixin.rangedweapon.attribute;

import com.ultra.megamod.lib.rangedweapon.RangedWeaponMod;
import net.minecraft.world.effect.MobEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MobEffects.class)
public class StatusEffectsMixin {
    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void static_tail_RangedWeaponAPI(CallbackInfo ci) {
        RangedWeaponMod.registerStatusEffects();
    }
}
