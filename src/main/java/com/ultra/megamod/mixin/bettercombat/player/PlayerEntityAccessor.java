package com.ultra.megamod.mixin.bettercombat.player;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Accessor for PlayerEntity inventory used by BetterCombat dual-wielding logic.
 * Ported 1:1 from BetterCombat (net.bettercombat.mixin.player.PlayerEntityAccessor).
 */
@Mixin(Player.class)
public interface PlayerEntityAccessor {
    @Accessor
    Inventory getInventory();
}
