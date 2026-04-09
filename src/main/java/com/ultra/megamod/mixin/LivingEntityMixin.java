package com.ultra.megamod.mixin;

import com.ultra.megamod.feature.adminmodules.AdminModuleState;
import com.ultra.megamod.feature.combat.animation.WeaponAttributeRegistry;
import com.ultra.megamod.feature.combat.animation.logic.knockback.ConfigurableKnockback;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin into LivingEntity to implement:
 * - Velocity: cancel knockback when Velocity module is active
 * - BetterCombat configurable knockback multiplier
 * - Two-handed weapon off-hand hiding
 * Ported 1:1 from BetterCombat's LivingEntityMixin.
 */
@Mixin(LivingEntity.class)
public class LivingEntityMixin implements ConfigurableKnockback {

    @Unique
    private float megamod$knockbackMultiplier = 1.0f;

    @Override
    public void setKnockbackMultiplier_BetterCombat(float strength) {
        this.megamod$knockbackMultiplier = strength;
    }

    /**
     * Cancel knockback for Velocity module OR apply BetterCombat multiplier.
     */
    @Inject(method = "knockback", at = @At("HEAD"), cancellable = true)
    private void megamod$modifyKnockback(double strength, double x, double z, CallbackInfo ci) {
        if (AdminModuleState.velocityEnabled) {
            ci.cancel();
            return;
        }
        // Apply BetterCombat knockback multiplier (will be reset after attack)
        if (megamod$knockbackMultiplier != 1.0f) {
            var self = (LivingEntity) (Object) this;
            // Cancel original and apply with multiplier
            ci.cancel();
            double modified = strength * megamod$knockbackMultiplier;
            megamod$knockbackMultiplier = 1.0f; // Reset after use
            if (modified > 0) {
                self.hurtMarked = true;
                var vel = self.getDeltaMovement();
                var push = new net.minecraft.world.phys.Vec3(x, 0, z).normalize().scale(modified);
                self.setDeltaMovement(vel.x / 2.0 - push.x, self.onGround() ? Math.min(0.4, vel.y / 2.0 + modified) : vel.y, vel.z / 2.0 - push.z);
            }
        }
    }

    /**
     * Two-handed weapon off-hand hiding: when a player holds a two-handed weapon,
     * their off-hand slot returns empty to prevent shields/items in off-hand.
     * Ported from BetterCombat's LivingEntityMixin.getEquippedStack_Pre.
     */
    @Inject(method = "getItemBySlot", at = @At("HEAD"), cancellable = true)
    private void megamod$hideOffHandForTwoHanded(EquipmentSlot slot, CallbackInfoReturnable<ItemStack> cir) {
        if (slot != EquipmentSlot.OFFHAND) return;
        var self = (LivingEntity) (Object) this;
        if (!(self instanceof Player player)) return;

        var mainAttrs = WeaponAttributeRegistry.getAttributes(player.getMainHandItem());
        if (mainAttrs != null && mainAttrs.twoHanded()) {
            cir.setReturnValue(ItemStack.EMPTY);
        }
    }
}
