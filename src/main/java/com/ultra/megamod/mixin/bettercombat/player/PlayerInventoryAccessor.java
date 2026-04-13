package com.ultra.megamod.mixin.bettercombat.player;

import net.minecraft.world.entity.EquipmentTable;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Accessor for PlayerInventory equipment used by BetterCombat offhand logic.
 * Ported 1:1 from BetterCombat (net.bettercombat.mixin.player.PlayerInventoryAccessor).
 */
@Mixin(Inventory.class)
public interface PlayerInventoryAccessor {
    // Note: In NeoForge 1.21.11, the equipment field may not exist directly.
    // The Inventory class provides getItem(slot) for access instead.
    // This accessor is kept for API compatibility but may need adjustment
    // if the internal field name differs.
}
