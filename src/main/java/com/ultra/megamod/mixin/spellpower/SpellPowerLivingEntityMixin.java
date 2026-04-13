package com.ultra.megamod.mixin.spellpower;

import com.ultra.megamod.lib.spellpower.api.ModifierDefinitions;
import com.ultra.megamod.lib.spellpower.api.SpellPowerMechanics;
import com.ultra.megamod.lib.spellpower.api.SpellResistance;
import com.ultra.megamod.lib.spellpower.api.SpellSchools;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
abstract class SpellPowerLivingEntityMixin extends Entity {
    SpellPowerLivingEntityMixin(final EntityType<?> type, final Level world) {
        super(type, world);
    }

    // init tail - add innate modifiers for mechanics attributes
    @Inject(method = "<init>", at = @At("TAIL"))
    private void spellpower$onConstructed(EntityType entityType, Level world, CallbackInfo ci) {
        var entity = (LivingEntity)(Object)this;
        var attributes = entity.getAttributes();
        for (var mechanic : SpellPowerMechanics.all.values()) {
            if (mechanic.innateModifier != null && mechanic.deferredHolder != null) {
                var instance = attributes.getInstance(mechanic.deferredHolder);
                if (instance != null && instance.getModifier(ModifierDefinitions.INNATE_BONUS) == null) {
                    instance.addPermanentModifier(mechanic.innateModifier);
                }
            }
        }
    }

    @ModifyVariable(method = "hurtServer", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private float spellpower$damage_resistance(float amount, ServerLevel serverLevel, DamageSource source) {
        var entity = (LivingEntity)(Object)this;
        if (entity.isInvulnerableTo(serverLevel, source) || entity.isDeadOrDying()) {
            return amount;
        }
        return (float) SpellResistance.resist(entity, amount, source);
    }
}
