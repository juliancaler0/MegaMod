package com.ultra.megamod.feature.baritone.movement.movements;

import com.ultra.megamod.feature.baritone.movement.*;
import com.ultra.megamod.feature.baritone.pathfinding.BetterBlockPos;
import com.ultra.megamod.feature.baritone.pathfinding.CalculationContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 * Jump up 1 block (move horizontally 1 + vertically 1).
 */
public class MovementAscend extends Movement {
    public MovementAscend(CalculationContext ctx, BetterBlockPos src, BetterBlockPos dest) {
        super(src, dest);
    }

    @Override
    public double calculateCost(CalculationContext ctx) {
        // dest is 1 up + 1 horizontal from src
        BlockPos destFeet = new BlockPos(dest.x, dest.y, dest.z);
        BlockPos destHead = destFeet.above();
        BlockPos destFloor = destFeet.below(); // This is at src.y level
        BlockPos aboveSrcHead = new BlockPos(src.x, src.y + 2, src.z); // Need headroom to jump

        // Need headroom above src to jump
        if (!MovementHelper.canWalkThrough(ctx, aboveSrcHead)) {
            if (!ctx.allowBreak || !MovementHelper.canBreak(ctx, aboveSrcHead)) {
                return ActionCosts.COST_INF;
            }
            return ActionCosts.WALK_ONE_BLOCK_COST + ActionCosts.JUMP_PENALTY + MovementHelper.getBreakCost(ctx, aboveSrcHead);
        }

        // Dest feet and head must be clear
        if (!MovementHelper.canWalkThrough(ctx, destFeet)) {
            if (!ctx.allowBreak || !MovementHelper.canBreak(ctx, destFeet)) {
                return ActionCosts.COST_INF;
            }
            return ActionCosts.WALK_ONE_BLOCK_COST + ActionCosts.JUMP_PENALTY + MovementHelper.getBreakCost(ctx, destFeet);
        }
        if (!MovementHelper.canWalkThrough(ctx, destHead)) {
            if (!ctx.allowBreak || !MovementHelper.canBreak(ctx, destHead)) {
                return ActionCosts.COST_INF;
            }
            return ActionCosts.WALK_ONE_BLOCK_COST + ActionCosts.JUMP_PENALTY + MovementHelper.getBreakCost(ctx, destHead);
        }

        // Must have solid floor at dest
        if (!MovementHelper.canWalkOn(ctx, destFloor)) {
            return ActionCosts.COST_INF;
        }

        if (MovementHelper.isDanger(ctx, destFeet)) return ActionCosts.COST_INF;

        return ActionCosts.WALK_ONE_BLOCK_COST + ActionCosts.JUMP_PENALTY;
    }

    @Override
    public MovementState.Status tick(ServerPlayer player, ServerLevel level) {
        state.incrementTick();

        // Clear blocks if needed
        BlockPos destFeet = new BlockPos(dest.x, dest.y, dest.z);
        BlockPos destHead = destFeet.above();
        BlockPos aboveSrc = new BlockPos(src.x, src.y + 2, src.z);
        if (!MovementHelper.canWalkThrough(level, aboveSrc)) breakBlock(level, player, aboveSrc);
        if (!MovementHelper.canWalkThrough(level, destFeet)) breakBlock(level, player, destFeet);
        if (!MovementHelper.canWalkThrough(level, destHead)) breakBlock(level, player, destHead);

        if (isAtPosition(player, dest)) {
            return MovementState.Status.SUCCESS;
        }

        // Jump when on ground and near src
        if (player.onGround() && state.getTicksInCurrent() < 5) {
            jump(player);
        }

        moveTowards(player, centerX(), centerY(), centerZ(), 0.18);

        if (state.getTicksInCurrent() > 60) {
            return MovementState.Status.FAILED;
        }
        return MovementState.Status.RUNNING;
    }
}
