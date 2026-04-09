package com.ultra.megamod.feature.baritone.safety;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.component.DataComponents;

/**
 * Inventory utility for bot operations — check materials, find tools, count items.
 */
public class InventoryHelper {

    /**
     * Count how many of a specific block item the player has.
     */
    public static int countBlock(ServerPlayer player, Block block) {
        int count = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.getItem() instanceof BlockItem bi && bi.getBlock() == block) {
                count += stack.getCount();
            }
        }
        return count;
    }

    /**
     * Count total placeable blocks in inventory.
     */
    public static int countPlaceableBlocks(ServerPlayer player) {
        int count = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.getItem() instanceof BlockItem) {
                count += stack.getCount();
            }
        }
        return count;
    }

    /**
     * Check if the player has a water bucket.
     */
    public static boolean hasWaterBucket(ServerPlayer player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (player.getInventory().getItem(i).is(Items.WATER_BUCKET)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Find the best tool for breaking a specific block state.
     * Returns the slot index, or -1 if no suitable tool found.
     */
    public static int findBestTool(ServerPlayer player, BlockState state) {
        int bestSlot = -1;
        float bestSpeed = 1.0f;
        int bestDurability = 0;

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.isEmpty()) continue;
            float speed = stack.getDestroySpeed(state);
            if (speed > bestSpeed || (speed == bestSpeed && stack.getMaxDamage() - stack.getDamageValue() > bestDurability)) {
                bestSpeed = speed;
                bestSlot = i;
                bestDurability = stack.getMaxDamage() - stack.getDamageValue();
            }
        }
        return bestSlot;
    }

    /**
     * Check if the player has enough of a specific item.
     */
    public static boolean hasItem(ServerPlayer player, Item item, int count) {
        int found = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(item)) {
                found += stack.getCount();
                if (found >= count) return true;
            }
        }
        return false;
    }

    /**
     * Count a specific item in inventory.
     */
    public static int countItem(ServerPlayer player, Item item) {
        int count = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(item)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    /**
     * Check if the player has any food items.
     */
    public static boolean hasFood(ServerPlayer player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.has(DataComponents.FOOD)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get a summary of the player's inventory for display.
     */
    public static String getInventorySummary(ServerPlayer player) {
        int totalItems = 0;
        int emptySlots = 0;
        int tools = 0;
        int blocks = 0;

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.isEmpty()) {
                emptySlots++;
            } else {
                totalItems += stack.getCount();
                if (stack.getMaxDamage() > 0) tools++; // Damageable = tool/weapon/armor
                if (stack.getItem() instanceof BlockItem) blocks += stack.getCount();
            }
        }
        return totalItems + " items, " + emptySlots + " empty, " + tools + " tools, " + blocks + " blocks";
    }
}
