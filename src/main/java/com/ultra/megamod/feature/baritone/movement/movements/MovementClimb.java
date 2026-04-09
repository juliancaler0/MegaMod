package com.ultra.megamod.feature.baritone.movement.movements;

import com.ultra.megamod.feature.baritone.movement.*;
import com.ultra.megamod.feature.baritone.pathfinding.BetterBlockPos;
import com.ultra.megamod.feature.baritone.pathfinding.CalculationContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 * Climb up or down ladders, vines, and other climbable blocks.
 */
public class MovementClimb extends Movement {
    private final boolean ascending;

    public MovementClimb(CalculationContext ctx, BetterBlockPos src, BetterBlockPos dest, boolean ascending) {
        super(src, dest);
        this.ascending = ascending;
    }

    @Override
    public double calculateCost(CalculationContext ctx) {
        if (!ctx.settings.allowClimb) return ActionCosts.COST_INF;

        if (ascending) {
            // Climbing up: src must be climbable, dest head must be clear
            BlockPos srcFeet = new BlockPos(src.x, src.y, src.z);
            BlockPos destFeet = new BlockPos(dest.x, dest.y, dest.z);
            BlockPos destHead = destFeet.above();

            if (!MovementHelper.isClimbable(ctx, srcFeet) && !MovementHelper.isClimbable(ctx, destFeet)) {
                return ActionCosts.COST_INF;
            }

            if (!MovementHelper.canWalkThrough(ctx, destHead)) {
                if (!ctx.allowBreak || !MovementHelper.canBreak(ctx, destHead)) {
                    return ActionCosts.COST_INF;
                }
                return ActionCosts.CLIMB_ONE_BLOCK_COST + MovementHelper.getBreakCost(ctx, destHead);
            }

            return ActionCosts.CLIMB_ONE_BLOCK_COST;
        } else {
            // Climbing down: dest must be climbable or have floor
            BlockPos destFeet = new BlockPos(dest.x, dest.y, dest.z);

            if (!MovementHelper.isClimbable(ctx, destFeet)) {
                // Need floor to stop
                BlockPos destFloor = destFeet.below();
                if (!MovementHelper.canWalkOn(ctx, destFloor)) {
                    return ActionCosts.COST_INF;
                }
            }

            if (MovementHelper.isDanger(ctx, destFeet)) {
                return ActionCosts.COST_INF;
            }

            return ActionCosts.CLIMB_ONE_BLOCK_COST;
        }
    }

    @Override
    public MovementState.Status tick(ServerPlayer player, ServerLevel level) {
        state.incrementTick();

        if (isAtPosition(player, dest)) {
            return MovementState.Status.SUCCESS;
        }

        // Move vertically — stay centered on the climbable block
        double speed = ascending ? 0.12 : 0.15;
        moveTowards(player, src.x + 0.5, centerY(), src.z + 0.5, speed);

        if (state.getTicksInCurrent() > 60) {
            return MovementState.Status.FAILED;
        }
        return MovementState.Status.RUNNING;
    }
}
