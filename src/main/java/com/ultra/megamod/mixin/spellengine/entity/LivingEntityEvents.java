package com.ultra.megamod.mixin.spellengine.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import com.ultra.megamod.lib.spellengine.api.event.CombatEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityEvents {
    /**
     * In 1.21.11, onAttacking(Entity) was renamed to setLastHurtMob(Entity).
     * We inject here to capture the attack target for combat events.
     */
    @Inject(method = "setLastHurtMob", at = @At("HEAD"))
    private void setLastHurtMob_HEAD_Event(Entity target, CallbackInfo ci) {
        var entity = (LivingEntity) (Object) this;
        if (CombatEvents.ENTITY_ANY_ATTACK.isListened()) {
            var args = new CombatEvents.EntityAttack.Args(entity, target);
            CombatEvents.ENTITY_ANY_ATTACK.invoke(listener -> listener.onEntityAttack(args));
        }
        if (entity instanceof Player player) {
            if (CombatEvents.PLAYER_ANY_ATTACK.isListened()) {
                var args = new CombatEvents.PlayerAttack.Args(player, target);
                CombatEvents.PLAYER_ANY_ATTACK.invoke(listener -> listener.onPlayerAttack(args));
            }
        }
    }

    @Inject(method = "hurtServer", at = @At("RETURN"))
    private void hurtServer_RETURN_entity(ServerLevel level, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) {
            var entity = (LivingEntity) (Object) this;
            if (CombatEvents.ENTITY_DAMAGE_TAKEN.isListened()) {
                var args = new CombatEvents.EntityDamageTaken.Args(entity, source, amount);
                CombatEvents.ENTITY_DAMAGE_TAKEN.invoke(listener -> listener.onDamageTaken(args));
            }
            // PLAYER_DAMAGE_TAKEN is now dispatched by TriggerEventHandlers via
            // LivingDamageEvent.Post (Phase A.4) to avoid double-firing.
        }
    }

    @Inject(method = "updatingUsingItem", at = @At("HEAD"))
    private void updatingUsingItem_HEAD_Event(CallbackInfo ci) {
        var entity = (LivingEntity) (Object) this;
        if (CombatEvents.ITEM_USE.isListened()) {
            var args = new CombatEvents.ItemUse.Args(entity, CombatEvents.ItemUse.Stage.TICK);
            CombatEvents.ITEM_USE.invoke(listener -> listener.onItemUseStart(args));
        }
    }


    /**
     * `blockUsingItem` is called when a shield block happens (was `damageShield` in older versions).
     */
    @WrapOperation(
            method = "applyItemBlocking",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;blockUsingItem(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;)V")
    )
    private void hurtServer_WRAP_blockUsingItem(
            // Mixin parameters
            LivingEntity instance, ServerLevel level, LivingEntity attacker, Operation<Void> original,
            // Context parameters
            ServerLevel contextLevel, DamageSource source, float damageAmount
    ) {
        if (CombatEvents.ENTITY_SHIELD_BLOCK.isListened()) {
            var args = new CombatEvents.EntityShieldBlock.Args(instance, source, damageAmount);
            CombatEvents.ENTITY_SHIELD_BLOCK.invoke(listener -> listener.onShieldBlock(args));
        }
        // PLAYER_SHIELD_BLOCK is now dispatched by TriggerEventHandlers via
        // LivingShieldBlockEvent (Phase A.4) to avoid double-firing.
        original.call(instance, level, attacker);
    }
}
