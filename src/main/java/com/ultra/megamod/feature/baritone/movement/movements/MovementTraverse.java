package com.ultra.megamod.feature.baritone.movement.movements;

import com.ultra.megamod.feature.baritone.movement.*;
import com.ultra.megamod.feature.baritone.pathfinding.BetterBlockPos;
import com.ultra.megamod.feature.baritone.pathfinding.CalculationContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 * Walk 1 block forward on flat ground.
 */
public class MovementTraverse extends Movement {
    public MovementTraverse(CalculationContext ctx, BetterBlockPos src, BetterBlockPos dest) {
        super(src, dest);
    }

    @Override
    public double calculateCost(CalculationContext ctx) {
        BlockPos feetPos = new BlockPos(dest.x, dest.y, dest.z);
        BlockPos headPos = feetPos.above();
        BlockPos floorPos = feetPos.below();

        // Dest feet and head must be passable
        if (!MovementHelper.canWalkThrough(ctx, feetPos)) {
            if (!ctx.allowBreak || !MovementHelper.canBreak(ctx, feetPos)) {
                return ActionCosts.COST_INF;
            }
            return ActionCosts.WALK_ONE_BLOCK_COST + MovementHelper.getBreakCost(ctx, feetPos);
        }
        if (!MovementHelper.canWalkThrough(ctx, headPos)) {
            if (!ctx.allowBreak || !MovementHelper.canBreak(ctx, headPos)) {
                return ActionCosts.COST_INF;
            }
            return ActionCosts.WALK_ONE_BLOCK_COST + MovementHelper.getBreakCost(ctx, headPos);
        }

        // Must have floor
        if (!MovementHelper.canWalkOn(ctx, floorPos)) {
            return ActionCosts.COST_INF;
        }

        // Danger check
        if (MovementHelper.isDanger(ctx, feetPos) || MovementHelper.isDanger(ctx, floorPos)) {
            return ActionCosts.COST_INF;
        }

        return ctx.allowSprint ? ActionCosts.SPRINT_ONE_BLOCK_COST : ActionCosts.WALK_ONE_BLOCK_COST;
    }

    @Override
    public MovementState.Status tick(ServerPlayer player, ServerLevel level) {
        state.incrementTick();

        // Break blocks if needed
        BlockPos feetPos = new BlockPos(dest.x, dest.y, dest.z);
        BlockPos headPos = feetPos.above();
        if (!MovementHelper.canWalkThrough(level, feetPos)) breakBlock(level, player, feetPos);
        if (!MovementHelper.canWalkThrough(level, headPos)) breakBlock(level, player, headPos);

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
