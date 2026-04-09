package com.ultra.megamod.feature.combat.animation.logic;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Off-hand inventory access for attribute swapping during dual-wield attacks.
 * Ported 1:1 from BetterCombat (net.bettercombat.logic.InventoryUtil).
 */
public class InventoryUtil {

    private static final int OFFHAND_SLOT = 40; // Vanilla off-hand slot index

    public static ItemStack getOffHandSlotStack(Player player) {
        return player.getInventory().getItem(OFFHAND_SLOT);
    }

    public static void setOffHandSlotStack(Player player, ItemStack stack) {
        player.getInventory().setItem(OFFHAND_SLOT, stack);
    }
}
