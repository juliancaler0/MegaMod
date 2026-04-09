package com.ultra.megamod.feature.baritone.pathfinding;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;

/**
 * Calculates mining speed for a ServerPlayer based on their inventory tools.
 * Includes enchantment awareness (Efficiency, Silk Touch), durability tracking,
 * and cached lookups for repeated queries.
 */
public class ServerToolSet {
    private final ServerPlayer player;
    private final Map<Long, Double> breakTicksCache = new HashMap<>();
    private int bestToolSlot = -1;
    private float bestToolSpeed = 1.0f;
    private boolean hasSilkTouch = false;

    public ServerToolSet(ServerPlayer player) {
        this.player = player;
        analyzeTools();
    }

    /**
     * Scan the player's inventory to find the best tool and note enchantments.
     */
    private void analyzeTools() {
        bestToolSpeed = 1.0f;
        bestToolSlot = -1;
        hasSilkTouch = false;

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.isEmpty()) continue;

            // Check for silk touch on any tool
            ItemEnchantments enchantments = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
            if (!enchantments.isEmpty()) {
                // Check if any enchantment is silk touch
                enchantments.entrySet().forEach(entry -> {
                    if (entry.getKey().is(Enchantments.SILK_TOUCH)) {
                        hasSilkTouch = true;
                    }
                });
            }
        }
    }

    /**
     * Get the time in ticks to break a block, using cached results.
     * Returns -1 if the block is unbreakable.
     */
    public double getBreakTicks(ServerLevel level, BlockPos pos) {
        long key = BlockPos.asLong(pos.getX(), pos.getY(), pos.getZ());
        Double cached = breakTicksCache.get(key);
        if (cached != null) return cached;

        double result = calculateBreakTicks(level, pos);
        if (breakTicksCache.size() < 10000) { // Limit cache size
            breakTicksCache.put(key, result);
        }
        return result;
    }

    private double calculateBreakTicks(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        float hardness = state.getDestroySpeed(level, pos);
        if (hardness < 0) return -1; // Unbreakable

        // Find the best tool for this specific block
        float bestSpeed = 1.0f;
        int bestDurability = 0;

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.isEmpty()) continue;

            // Skip tools with 0 durability remaining
            if (stack.getMaxDamage() > 0 && stack.getDamageValue() >= stack.getMaxDamage()) continue;

            float speed = stack.getDestroySpeed(state);
            if (speed > bestSpeed) {
                bestSpeed = speed;
                bestDurability = stack.getMaxDamage() - stack.getDamageValue();
            }
        }

        // Efficiency enchantment is factored into getDestroySpeed already
        boolean canHarvest = !state.requiresCorrectToolForDrops() || bestSpeed > 1.0f;
        if (!canHarvest) {
            return hardness * 100.0; // Very slow without correct tool
        }
        return (hardness * 30.0) / bestSpeed;
    }

    /**
     * Find the best tool slot for a specific block state.
     * Returns -1 if bare hands are best.
     */
    public int findBestToolSlot(BlockState state) {
        int bestSlot = -1;
        float bestSpeed = 1.0f;

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.isEmpty()) continue;
            if (stack.getMaxDamage() > 0 && stack.getDamageValue() >= stack.getMaxDamage()) continue;

            float speed = stack.getDestroySpeed(state);
            if (speed > bestSpeed) {
                bestSpeed = speed;
                bestSlot = i;
            }
        }
        return bestSlot;
    }

    public boolean hasSilkTouch() { return hasSilkTouch; }

    /**
     * Clear the break ticks cache (call when inventory changes).
     */
    public void invalidateCache() {
        breakTicksCache.clear();
        analyzeTools();
    }
}
