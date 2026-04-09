package com.ultra.megamod.feature.baritone.movement.movements;

import com.ultra.megamod.feature.baritone.movement.*;
import com.ultra.megamod.feature.baritone.pathfinding.BetterBlockPos;
import com.ultra.megamod.feature.baritone.pathfinding.CalculationContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 * Step down 1 block (move horizontally 1 + vertically -1).
 */
public class MovementDescend extends Movement {
    public MovementDescend(CalculationContext ctx, BetterBlockPos src, BetterBlockPos dest) {
        super(src, dest);
    }

    @Override
    public double calculateCost(CalculationContext ctx) {
        BlockPos destFeet = new BlockPos(dest.x, dest.y, dest.z);
        BlockPos destHead = destFeet.above(); // This is src.y level
        BlockPos destAboveHead = new BlockPos(dest.x, dest.y + 2, dest.z);
        BlockPos destFloor = destFeet.below();

        // Need the 2 blocks at dest to be clear
        if (!MovementHelper.canWalkThrough(ctx, destFeet)) {
            if (!ctx.allowBreak || !MovementHelper.canBreak(ctx, destFeet)) {
                return ActionCosts.COST_INF;
            }
            return ActionCosts.WALK_ONE_BLOCK_COST + ActionCosts.FALL_ONE_BLOCK_COST + MovementHelper.getBreakCost(ctx, destFeet);
        }
        // destHead is at the same level as src feet - it's the block we step through horizontally
        if (!MovementHelper.canWalkThrough(ctx, destHead)) {
            if (!ctx.allowBreak || !MovementHelper.canBreak(ctx, destHead)) {
                return ActionCosts.COST_INF;
            }
            return ActionCosts.WALK_ONE_BLOCK_COST + ActionCosts.FALL_ONE_BLOCK_COST + MovementHelper.getBreakCost(ctx, destHead);
        }

        // Must have floor
        if (!MovementHelper.canWalkOn(ctx, destFloor)) {
            return ActionCosts.COST_INF;
        }

        if (MovementHelper.isDanger(ctx, destFeet) || MovementHelper.isDanger(ctx, destFloor)) {
            return ActionCosts.COST_INF;
        }

        return ActionCosts.WALK_ONE_BLOCK_COST + ActionCosts.FALL_ONE_BLOCK_COST;
    }

    @Override
    public MovementState.Status tick(ServerPlayer player, ServerLevel level) {
        state.incrementTick();

        BlockPos destFeet = new BlockPos(dest.x, dest.y, dest.z);
        BlockPos destHead = destFeet.above();
        if (!MovementHelper.canWalkThrough(level, destFeet)) breakBlock(level, player, destFeet);
        if (!MovementHelper.canWalkThrough(level, destHead)) breakBlock(level, player, destHead);

        if (isAtPosition(player, dest)) {
            return MovementState.Status.SUCCESS;
        }

        moveTowards(player, centerX(), centerY(), centerZ(), 0.21);

        if (state.getTicksInCurrent() > 60) {
            return MovementState.Status.FAILED;
        }
        return MovementState.Status.RUNNING;
    }
}
