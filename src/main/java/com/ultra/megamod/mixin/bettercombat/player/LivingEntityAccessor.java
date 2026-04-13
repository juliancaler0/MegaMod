package com.ultra.megamod.mixin.bettercombat.player;

import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Accessor for LivingEntity internals used by BetterCombat attack cooldown logic.
 * Ported 1:1 from BetterCombat (net.bettercombat.mixin.player.LivingEntityAccessor).
 */
@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {
    @Accessor("attackStrengthTicker")
    int bettercombat$getAttackStrengthTicker();

    @Accessor("attackStrengthTicker")
    void bettercombat$setAttackStrengthTicker(int lastAttackedTicks);

    // turnHead was removed in 1.21.11
    // @Invoker("turnHead")
    // float bettercombat$invokeTurnHead(float bodyRotation, float headRotation);
}
