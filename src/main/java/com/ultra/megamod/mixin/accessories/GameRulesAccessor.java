package com.ultra.megamod.mixin.accessories;

// TODO: 1.21.11 - GameRules inner classes (Value, Key, Type, Category, BooleanValue) were restructured
// This accessor needs to be rewritten for the new GameRules API.
// The accessories library uses this to register custom game rules.

import net.minecraft.world.level.gamerules.GameRules;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(GameRules.class)
public interface GameRulesAccessor {
    // Original methods:
    // @Invoker("register") static <T extends GameRules.Value<T>> GameRules.Key<T> accessories$register(...)
    // Inner mixin for BooleanValue.create
    // These are removed until the new GameRules API is understood.
}
