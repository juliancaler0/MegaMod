package com.ultra.megamod.mixin.spellengine.entity;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {
    @Accessor("lastHurt")
    float spellEngine_getLastDamageTaken();
    @Accessor("lastDamageSource")
    DamageSource spellEngine_getLastDamageSource();

    // turnHead was removed in 1.21.11
    // @Invoker("turnHead")
    // float spellEngine_invoke_TurnHead(float bodyRotation, float headRotation);

    @Accessor("attackStrengthTicker")
    int spellEngine_getLastAttackedTicks();
    @Accessor("attackStrengthTicker")
    void spellEngine_setLastAttackedTicks(int lastAttackedTicks);
}
