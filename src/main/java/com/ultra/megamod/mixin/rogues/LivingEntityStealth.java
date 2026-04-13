package com.ultra.megamod.mixin.rogues;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.ultra.megamod.feature.combat.rogues.RoguesMod;
import com.ultra.megamod.feature.combat.spell.SpellEffects;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin for stealth mechanics on LivingEntity.
 * Ported from net.rogues.mixin.LivingEntityStealth.
 *
 * - Makes stealthed entities appear invisible (hooks potion visibility check)
 * - Reduces visibility scaling factor for stealthed entities
 */
@Mixin(LivingEntity.class)
public class LivingEntityStealth {

    /**
     * Wraps the invisibility check in updateInvisibilityStatus to also consider
     * entities with the STEALTH effect as invisible.
     */
    @WrapOperation(
            method = "updateInvisibilityStatus",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;hasEffect(Lnet/minecraft/core/Holder;)Z")
    )
    private boolean updatePotionVisibility_WRAP_Stealth(LivingEntity instance, Holder<MobEffect> effect, Operation<Boolean> original) {
        return original.call(instance, effect) || instance.hasEffect(SpellEffects.STEALTH);
    }

    /**
     * Reduces the visibility percent for stealthed entities,
     * making them harder for mobs to detect at range.
     */
    @Inject(method = "getVisibilityPercent", at = @At("RETURN"), cancellable = true)
    private void getVisibilityPercent_RETURN_Stealth(Entity entity, CallbackInfoReturnable<Double> cir) {
        var thisEntity = (LivingEntity) (Object) this;
        if (thisEntity.hasEffect(SpellEffects.STEALTH)) {
            cir.setReturnValue(cir.getReturnValue() * RoguesMod.tweaksConfig.stealth_visibility_multiplier);
        }
    }
}
