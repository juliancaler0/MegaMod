package com.ultra.megamod.feature.baritone.movement.movements;

import com.ultra.megamod.feature.baritone.movement.*;
import com.ultra.megamod.feature.baritone.pathfinding.BetterBlockPos;
import com.ultra.megamod.feature.baritone.pathfinding.CalculationContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;

/**
 * MLG water bucket: place water at landing spot, fall, pick up water.
 * Allows safe falls from any height if the bot has a water bucket.
 */
public class MovementWaterBucket extends Movement {
    private final int fallHeight;
    private Phase phase = Phase.INIT;
    private int phaseTicks = 0;

    private enum Phase {
        INIT,           // Initial state
        PLACE_WATER,    // Place water at landing
        FALLING,        // Fall down
        PICKUP_WATER    // Pick up the water
    }

    public MovementWaterBucket(CalculationContext ctx, BetterBlockPos src, BetterBlockPos dest) {
        super(src, dest);
        this.fallHeight = src.y - dest.y;
    }

    @Override
    public double calculateCost(CalculationContext ctx) {
        if (!ctx.settings.allowWaterBucket) return ActionCosts.COST_INF;
        if (fallHeight <= ActionCosts.MAX_SAFE_FALL) return ActionCosts.COST_INF; // Use normal fall
        if (fallHeight > 60) return ActionCosts.COST_INF; // Too risky

        // Must be same X/Z (straight down)
        if (src.x != dest.x || src.z != dest.z) return ActionCosts.COST_INF;

        // Check all blocks in fall path are clear
        for (int dy = 0; dy < fallHeight; dy++) {
            BlockPos pos = new BlockPos(dest.x, src.y - dy, dest.z);
            if (!MovementHelper.canWalkThrough(ctx, pos)) {
                return ActionCosts.COST_INF;
            }
        }

        // Landing floor must be solid (to place water on)
        BlockPos landingFloor = new BlockPos(dest.x, dest.y - 1, dest.z);
        if (!MovementHelper.canWalkOn(ctx, landingFloor)) {
            return ActionCosts.COST_INF;
        }

        // Cost: fall time + water placement time + pickup time
        return fallHeight * ActionCosts.FALL_ONE_BLOCK_COST + 6.0;
    }

    @Override
    public MovementState.Status tick(ServerPlayer player, ServerLevel level) {
        state.incrementTick();
        phaseTicks++;

        switch (phase) {
            case INIT -> {
                // Check player has water bucket
                if (!hasWaterBucket(player)) {
                    return MovementState.Status.FAILED;
                }
                // Place water at landing
                BlockPos landingPos = new BlockPos(dest.x, dest.y, dest.z);
                level.setBlockAndUpdate(landingPos, Blocks.WATER.defaultBlockState());
                consumeWaterBucket(player);
                phase = Phase.PLACE_WATER;
                phaseTicks = 0;
            }
            case PLACE_WATER -> {
                // Start falling
                phase = Phase.FALLING;
                phaseTicks = 0;
            }
            case FALLING -> {
                // Move towards landing
                moveTowards(player, dest.x + 0.5, player.getY(), dest.z + 0.5, 0.1);

                // Check if we've landed (in or near water)
                if (player.getY() <= dest.y + 1.5 || player.isInWater()) {
                    teleportPlayer(player, dest.x + 0.5, dest.y, dest.z + 0.5);
                    phase = Phase.PICKUP_WATER;
                    phaseTicks = 0;
                }

                if (phaseTicks > fallHeight * 3 + 20) {
                    // Taking too long, pickup water and fail
                    pickupWater(level, player);
                    return MovementState.Status.FAILED;
                }
            }
            case PICKUP_WATER -> {
                // Pick up the water after a short delay
                if (phaseTicks >= 3) {
                    pickupWater(level, player);
                    return MovementState.Status.SUCCESS;
                }
            }
        }

        if (state.getTicksInCurrent() > 200) {
            // Safety timeout — try to pick up water
            pickupWater(level, player);
            return MovementState.Status.FAILED;
        }
        return MovementState.Status.RUNNING;
    }

    private boolean hasWaterBucket(ServerPlayer player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (player.getInventory().getItem(i).is(Items.WATER_BUCKET)) {
                return true;
            }
        }
        return false;
    }

    private void consumeWaterBucket(ServerPlayer player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(Items.WATER_BUCKET)) {
                player.getInventory().setItem(i, new ItemStack(Items.BUCKET));
                return;
            }
        }
    }

    private void pickupWater(ServerLevel level, ServerPlayer player) {
        // Remove water block and give back water bucket
        BlockPos waterPos = new BlockPos(dest.x, dest.y, dest.z);
        if (level.getBlockState(waterPos).getBlock() instanceof LiquidBlock) {
            level.setBlockAndUpdate(waterPos, Blocks.AIR.defaultBlockState());
            // Replace an empty bucket with water bucket
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                if (player.getInventory().getItem(i).is(Items.BUCKET)) {
                    player.getInventory().setItem(i, new ItemStack(Items.WATER_BUCKET));
                    return;
                }
            }
            // No empty bucket found — just give one
            player.getInventory().add(new ItemStack(Items.WATER_BUCKET));
        }
    }
}
