package com.ultra.megamod.feature.baritone.behavior;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Set;

/**
 * Smart inventory management for the bot. Handles tool selection, weapon
 * selection, food selection, junk disposal, and general inventory queries.
 */
public class InventoryBehavior {
    private ServerPlayer player;

    /** Items considered junk that can be dropped when inventory is full. */
    private static final Set<String> JUNK_ITEMS = Set.of(
        "cobblestone", "dirt", "gravel", "cobbled_deepslate",
        "andesite", "diorite", "granite", "tuff",
        "netherrack", "basalt", "blackstone",
        "sand", "red_sand", "clay_ball"
    );

    /** Low-durability threshold — don't select tools about to break. */
    private static final int DURABILITY_THRESHOLD = 10;

    public InventoryBehavior(ServerPlayer player) {
        this.player = player;
    }

    public void updatePlayer(ServerPlayer player) {
        this.player = player;
    }

    // ======================== Tool selection ========================

    /**
     * Select the best tool on the hotbar for mining a given block state.
     * Evaluates destroy speed and avoids nearly-broken tools.
     * Sets the player's selected hotbar slot.
     *
     * @param state the block state to be mined
     * @return the hotbar slot index selected, or -1 if no suitable tool found
     */
    public int selectBestToolFor(BlockState state) {
        if (player == null) return -1;

        int bestSlot = -1;
        float bestSpeed = -1f;

        // Search hotbar first (slots 0-8)
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.isEmpty()) continue;

            // Skip nearly-broken tools
            if (stack.getMaxDamage() > 0
                    && stack.getMaxDamage() - stack.getDamageValue() <= DURABILITY_THRESHOLD) {
                continue;
            }

            float speed = stack.getDestroySpeed(state);
            if (speed > bestSpeed) {
                bestSpeed = speed;
                bestSlot = i;
            }
        }

        // If we found a good tool, select it
        if (bestSlot >= 0 && bestSpeed > 1.0f) {
            swapToSlot(bestSlot);
            return bestSlot;
        }

        // Check if any hotbar tool is at least "correct" for drops even if slow
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.isEmpty()) continue;
            if (stack.getMaxDamage() > 0
                    && stack.getMaxDamage() - stack.getDamageValue() <= DURABILITY_THRESHOLD) {
                continue;
            }
            if (stack.isCorrectToolForDrops(state)) {
                swapToSlot(i);
                return i;
            }
        }

        return -1; // No suitable tool on hotbar
    }

    // ======================== Weapon selection ========================

    /**
     * Select the highest-damage weapon on the hotbar. Prefers swords,
     * then axes, then anything with an attack damage component.
     *
     * @return the hotbar slot index selected, or -1 if nothing useful found
     */
    public int selectWeapon() {
        if (player == null) return -1;

        int bestSlot = -1;
        double bestDamage = 0;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.isEmpty()) continue;

            // Skip nearly broken
            if (stack.getMaxDamage() > 0
                    && stack.getMaxDamage() - stack.getDamageValue() <= DURABILITY_THRESHOLD) {
                continue;
            }

            Item item = stack.getItem();
            double damage = 0;

            // Swords and axes are the primary weapons
            String itemPath = BuiltInRegistries.ITEM.getKey(item).getPath();
            if (itemPath.contains("sword")) {
                damage = 9; // Swords are preferred weapons
            } else if (itemPath.contains("axe") && !itemPath.contains("pickaxe")) {
                damage = 6; // Axes do decent damage
            } else if (itemPath.contains("pickaxe") || itemPath.contains("shovel")) {
                damage = 2; // Other tools in a pinch
            }

            if (damage > bestDamage) {
                bestDamage = damage;
                bestSlot = i;
            }
        }

        if (bestSlot >= 0) {
            swapToSlot(bestSlot);
        }
        return bestSlot;
    }

    // ======================== Inventory queries ========================

    /**
     * Check if the player's inventory is full (no empty slots in main inventory + hotbar).
     */
    public boolean isInventoryFull() {
        if (player == null) return true;
        for (int i = 0; i < 36; i++) { // 0-8 hotbar, 9-35 main inventory
            if (player.getInventory().getItem(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check if the player has a specific item anywhere in their inventory.
     */
    public boolean hasItem(Item item) {
        if (player == null) return false;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (player.getInventory().getItem(i).is(item)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Count total quantity of a specific item across the entire inventory.
     */
    public int getItemCount(Item item) {
        if (player == null) return 0;
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
     * Find the first empty slot in the inventory.
     *
     * @return slot index, or -1 if inventory is full
     */
    public int findEmptySlot() {
        if (player == null) return -1;
        for (int i = 0; i < 36; i++) {
            if (player.getInventory().getItem(i).isEmpty()) {
                return i;
            }
        }
        return -1;
    }

    // ======================== Junk management ========================

    /**
     * Drop common junk items (cobblestone, dirt, gravel, etc.) to free inventory space.
     * Only drops from the main inventory, never the hotbar.
     *
     * @return the number of stacks dropped
     */
    public int dropJunkItems() {
        if (player == null) return 0;
        int dropped = 0;
        // Only drop from main inventory (9-35), leave hotbar alone
        for (int i = 9; i < 36; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.isEmpty()) continue;

            String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
            if (JUNK_ITEMS.contains(path)) {
                player.drop(stack.copy(), false);
                player.getInventory().setItem(i, ItemStack.EMPTY);
                dropped++;
            }
        }
        return dropped;
    }

    // ======================== Food selection ========================

    /**
     * Find and select the best food item on the hotbar.
     * "Best" = highest nutrition value.
     *
     * @return the hotbar slot index, or -1 if no food found on hotbar
     */
    public int selectFood() {
        if (player == null) return -1;

        int bestSlot = -1;
        int bestNutrition = 0;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.isEmpty()) continue;
            if (!stack.has(DataComponents.FOOD)) continue;

            var food = stack.get(DataComponents.FOOD);
            if (food != null) {
                int nutrition = food.nutrition();
                if (nutrition > bestNutrition) {
                    bestNutrition = nutrition;
                    bestSlot = i;
                }
            }
        }

        if (bestSlot >= 0) {
            swapToSlot(bestSlot);
        }
        return bestSlot;
    }

    /**
     * Check if the player has any food items anywhere in inventory.
     */
    public boolean hasFood() {
        if (player == null) return false;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.has(DataComponents.FOOD)) {
                return true;
            }
        }
        return false;
    }

    // ======================== Block checks ========================

    /**
     * Check if the player has any placeable block items in inventory.
     */
    public boolean hasBlocks() {
        if (player == null) return false;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.getItem() instanceof BlockItem) {
                return true;
            }
        }
        return false;
    }

    /**
     * Count total placeable blocks across the entire inventory.
     */
    public int countBlocks() {
        if (player == null) return 0;
        int count = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.getItem() instanceof BlockItem) {
                count += stack.getCount();
            }
        }
        return count;
    }

    // Helper: swap item at target hotbar slot to mainhand
    private void swapToSlot(int targetSlot) {
        if (player == null || targetSlot < 0 || targetSlot > 8) return;
        ItemStack mainHand = player.getMainHandItem();
        // Find which slot currently holds the mainhand item
        int currentSlot = -1;
        for (int i = 0; i < 9; i++) {
            if (player.getInventory().getItem(i) == mainHand) {
                currentSlot = i;
                break;
            }
        }
        if (currentSlot == targetSlot) return; // Already selected
        if (currentSlot >= 0) {
            // Swap the items
            ItemStack target = player.getInventory().getItem(targetSlot);
            player.getInventory().setItem(currentSlot, target);
            player.getInventory().setItem(targetSlot, mainHand);
        }
        player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, player.getInventory().getItem(targetSlot));
    }
}
