package com.ultra.megamod.mixin.bettercombat.player;

import com.ultra.megamod.feature.combat.animation.api.PlayerAttackProperties;
import com.ultra.megamod.feature.combat.animation.logic.PlayerAttackHelper;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * LivingEntity mixin for BetterCombat dual-wield attribute swapping.
 * Swaps attribute values to off-hand weapon values when attacking with off-hand in a combo.
 *
 * Note: ConfigurableKnockback and two-handed offhand hiding are already in the existing
 * com.ultra.megamod.mixin.LivingEntityMixin -- only the attribute swap is here to avoid duplication.
 *
 * Ported from BetterCombat (net.bettercombat.mixin.player.LivingEntityMixin).
 */
@Mixin(LivingEntity.class)
public class BetterCombatLivingEntityMixin {

    // FEATURE: Dual wielded attacking - Client side weapon cooldown for offhand

    @Inject(method = "getAttributeValue(Lnet/minecraft/core/Holder;)D", at = @At("HEAD"), cancellable = true)
    public void bettercombat$getAttributeValue_Inject(Holder<Attribute> attribute, CallbackInfoReturnable<Double> cir) {
        var object = (Object) this;
        if (object instanceof Player player) {
            var comboCount = ((PlayerAttackProperties) player).getComboCount();
            if (player.level().isClientSide()
                    && comboCount > 0
                    && PlayerAttackHelper.shouldAttackWithOffHand(player, comboCount)) {
                PlayerAttackHelper.swapHandAttributes(player, true, () -> {
                    var value = player.getAttributes().getValue(attribute);
                    cir.setReturnValue(value);
                });
                cir.cancel();
            }
        }
    }
}
