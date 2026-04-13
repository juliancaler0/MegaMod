package com.ultra.megamod.mixin.combatroll;

import com.ultra.megamod.lib.combatroll.api.CombatRoll;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
abstract class CombatRollPlayerMixin {
    @Inject(
            method = "createAttributes",
            require = 1, allow = 1, at = @At("RETURN"))
    private static void combatroll$addAttributes(CallbackInfoReturnable<AttributeSupplier.Builder> cir) {
        for (var attribute : CombatRoll.Attributes.all) {
            if (attribute.entry != null) {
                cir.getReturnValue().add(attribute.entry);
            }
        }
    }
}
