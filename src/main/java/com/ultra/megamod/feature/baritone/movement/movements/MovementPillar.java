package com.ultra.megamod.feature.baritone.movement.movements;

import com.ultra.megamod.feature.baritone.movement.*;
import com.ultra.megamod.feature.baritone.pathfinding.BetterBlockPos;
import com.ultra.megamod.feature.baritone.pathfinding.CalculationContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;

/**
 * Pillar up: jump + place block below (straight up 1 block).
 */
public class MovementPillar extends Movement {
    public MovementPillar(CalculationContext ctx, BetterBlockPos src) {
        super(src, new BetterBlockPos(src.x, src.y + 1, src.z));
    }

    @Override
    public double calculateCost(CalculationContext ctx) {
        if (!ctx.allowPlace) return ActionCosts.COST_INF;

        BlockPos above = new BlockPos(dest.x, dest.y, dest.z);
        BlockPos aboveHead = above.above();

        // Head space at dest must be clear
        if (!MovementHelper.canWalkThrough(ctx, aboveHead)) {
            if (!ctx.allowBreak || !MovementHelper.canBreak(ctx, aboveHead)) {
                return ActionCosts.COST_INF;
            }
            return ActionCosts.WALK_ONE_BLOCK_COST + ActionCosts.JUMP_PENALTY + ActionCosts.PLACE_BLOCK_COST
                    + MovementHelper.getBreakCost(ctx, aboveHead);
        }

        // Need at least one placeable block in inventory (approximation)
        return ActionCosts.WALK_ONE_BLOCK_COST + ActionCosts.JUMP_PENALTY + ActionCosts.PLACE_BLOCK_COST;
    }

    @Override
    public MovementState.Status tick(ServerPlayer player, ServerLevel level) {
        state.incrementTick();

        if (isAtPosition(player, dest)) {
            return MovementState.Status.SUCCESS;
        }

        // Break block above head if needed
        BlockPos aboveHead = new BlockPos(dest.x, dest.y + 1, dest.z);
        if (!MovementHelper.canWalkThrough(level, aboveHead)) {
            breakBlock(level, player, aboveHead);
        }

        // Jump and place block at our FEET position (src.y), not below src
        if (player.onGround()) {
            // Place block at the position we're standing (our feet) to pillar up
            BlockPos placePos = new BlockPos(src.x, src.y, src.z);
            if (level.getBlockState(placePos).isAir()) {
                // Find a placeable block in inventory and consume it
                for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
                    net.minecraft.world.item.ItemStack stack = player.getInventory().getItem(slot);
                    if (!stack.isEmpty() && stack.getItem() instanceof net.minecraft.world.item.BlockItem blockItem) {
                        placeBlock(level, placePos, blockItem.getBlock().defaultBlockState());
                        stack.shrink(1);
                        break;
                    }
                }
            }
            jump(player);
        }

        // Center on the block
        moveTowards(player, src.x + 0.5, player.getY(), src.z + 0.5, 0.05);

        if (state.getTicksInCurrent() > 40) {
            return MovementState.Status.FAILED;
        }
        return MovementState.Status.RUNNING;
    }
}
