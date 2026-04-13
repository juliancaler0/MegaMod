package com.ultra.megamod.mixin.combatroll;

import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Player.class)
public interface CombatRollPlayerAccessor {
    @Invoker("isImmobile")
    boolean combatroll$invokeIsImmobile();
}
