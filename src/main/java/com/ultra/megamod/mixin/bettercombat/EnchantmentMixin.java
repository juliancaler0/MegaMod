package com.ultra.megamod.mixin.bettercombat;

import com.ultra.megamod.feature.combat.animation.api.PlayerAttackProperties;
import com.ultra.megamod.feature.combat.animation.logic.PlayerAttackHelper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

/**
 * Fixes enchantment slot resolution for dual-wield attacks so off-hand enchants apply correctly.
 * Ported 1:1 from BetterCombat (net.bettercombat.mixin.EnchantmentMixin).
 */
@Mixin(Enchantment.class)
public class EnchantmentMixin {

    @Inject(method = "getSlotItems", at = @At("RETURN"), cancellable = true)
    private static void bettercombat$getSlotItemsFix(LivingEntity entity, CallbackInfoReturnable<Map<EquipmentSlot, ItemStack>> cir) {
        if (entity instanceof Player player) {
            var comboCount = ((PlayerAttackProperties) player).getComboCount();
            var currentHand = PlayerAttackHelper.getCurrentAttack(player, comboCount);
            if (currentHand != null && currentHand.isOffHand()) {
                var map = cir.getReturnValue();
                if (map.get(EquipmentSlot.MAINHAND) != null) {
                    map.remove(EquipmentSlot.MAINHAND);
                }
                var offHandStack = player.getOffhandItem();
                if (!offHandStack.isEmpty()) {
                    map.put(EquipmentSlot.OFFHAND, offHandStack);
                }
                cir.setReturnValue(map);
            }
        }
    }
}
