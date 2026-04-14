package com.ultra.megamod.mixin.spellengine.effect;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.core.Holder;
import com.ultra.megamod.lib.spellengine.api.entity.SpellEngineAttributes;
import com.ultra.megamod.lib.spellengine.api.event.CombatEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityHealthImpacting {
    @Shadow public abstract double getAttributeValue(Holder<Attribute> attribute);

    @ModifyVariable(method = "heal", at = @At("HEAD"), argsOnly = true)
    private float modifyHealingTaken_SpellEngine(float amount) {
        return amount * (float) SpellEngineAttributes.HEALING_TAKEN
                .asMultiplier(getAttributeValue(SpellEngineAttributes.HEALING_TAKEN.entry));
    }

    @ModifyVariable(method = "hurtServer", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    public float modifyDamageTaken_SpellEngine(float amount) {
        return amount * (float) SpellEngineAttributes.DAMAGE_TAKEN
                .asMultiplier(getAttributeValue(SpellEngineAttributes.DAMAGE_TAKEN.entry));
    }

    @WrapOperation(
            method = "hurtServer",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;actuallyHurt(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)V")
    )
    private void hurtServer_ActuallyHurt_entity(
            // Mixin parameters
            LivingEntity instance, ServerLevel level, DamageSource source, float amount, Operation<Void> original
    ) {
        if (CombatEvents.ENTITY_DAMAGE_INCOMING.isListened()) {
            var args = new CombatEvents.EntityDamageTaken.Args(instance, source, amount);
            CombatEvents.ENTITY_DAMAGE_INCOMING.invoke(listener -> listener.onDamageTaken(args));
        }
        // PLAYER_DAMAGE_INCOMING is now dispatched by TriggerEventHandlers via
        // LivingIncomingDamageEvent (Phase A.4) to avoid double-firing.

        original.call(instance, level, source, amount);
    }
}
