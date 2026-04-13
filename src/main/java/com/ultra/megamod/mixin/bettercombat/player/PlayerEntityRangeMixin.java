package com.ultra.megamod.mixin.bettercombat.player;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.ultra.megamod.feature.combat.animation.logic.PlayerAttackHelper;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Modifies getEntityInteractionRange to include BetterCombat weapon range bonuses.
 * Ported 1:1 from BetterCombat (net.bettercombat.mixin.player.PlayerEntityRangeMixin).
 */
@Mixin(Player.class)
public class PlayerEntityRangeMixin {

    @WrapOperation(
            method = "getEntityInteractionRange",
            require = 0,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getAttributeValue(Lnet/minecraft/core/Holder;)D")
    )
    private double bettercombat$getEntityInteractionRange_Wrapped(Player instance, Holder<Attribute> attribute, Operation<Double> original) {
        var originalResult = original.call(instance, attribute);
        return PlayerAttackHelper.getRangeWithWeapon(instance, originalResult);
    }
}
