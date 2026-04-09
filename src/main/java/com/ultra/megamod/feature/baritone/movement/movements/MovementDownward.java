package com.ultra.megamod.feature.baritone.movement.movements;

import com.ultra.megamod.feature.baritone.movement.*;
import com.ultra.megamod.feature.baritone.pathfinding.BetterBlockPos;
import com.ultra.megamod.feature.baritone.pathfinding.CalculationContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 * Mine/descend straight down 1 block.
 */
public class MovementDownward extends Movement {
    public MovementDownward(CalculationContext ctx, BetterBlockPos src) {
        super(src, new BetterBlockPos(src.x, src.y - 1, src.z));
    }

    @Override
    public double calculateCost(CalculationContext ctx) {
        if (!ctx.allowBreak) return ActionCosts.COST_INF;

        BlockPos below = new BlockPos(dest.x, dest.y, dest.z);
        BlockPos belowBelow = below.below();

        // Must be able to break the block below
        if (!MovementHelper.canBreak(ctx, below)) {
            return ActionCosts.COST_INF;
        }

        // Must have a floor under that
        if (!MovementHelper.canWalkOn(ctx, belowBelow)) {
            return ActionCosts.COST_INF;
        }

        if (MovementHelper.isDanger(ctx, below) || MovementHelper.isDanger(ctx, belowBelow)) {
            return ActionCosts.COST_INF;
        }

        return ActionCosts.FALL_ONE_BLOCK_COST + MovementHelper.getBreakCost(ctx, below);
    }

    @Override
    public MovementState.Status tick(ServerPlayer player, ServerLevel level) {
        state.incrementTick();

        BlockPos below = new BlockPos(dest.x, dest.y, dest.z);

        // Break the block below us
        if (!MovementHelper.canWalkThrough(level, below)) {
            breakBlock(level, player, below);
            return MovementState.Status.RUNNING;
        }

        if (isAtPosition(player, dest)) {
            return MovementState.Status.SUCCESS;
        }

        // Stay centered and let gravity pull us down
        moveTowards(player, src.x + 0.5, player.getY(), src.z + 0.5, 0.05);

        if (state.getTicksInCurrent() > 60) {
            return MovementState.Status.FAILED;
        }
        return MovementState.Status.RUNNING;
    }
}
