package com.ultra.megamod.feature.baritone.movement.movements;

import com.ultra.megamod.feature.baritone.movement.*;
import com.ultra.megamod.feature.baritone.pathfinding.BetterBlockPos;
import com.ultra.megamod.feature.baritone.pathfinding.CalculationContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 * Diagonal movement (1 block in both X and Z simultaneously).
 */
public class MovementDiagonal extends Movement {
    private final int dx, dz;

    public MovementDiagonal(CalculationContext ctx, BetterBlockPos src, int dx, int dz) {
        super(src, new BetterBlockPos(src.x + dx, src.y, src.z + dz));
        this.dx = dx;
        this.dz = dz;
    }

    @Override
    public double calculateCost(CalculationContext ctx) {
        BlockPos destFeet = new BlockPos(dest.x, dest.y, dest.z);
        BlockPos destHead = destFeet.above();
        BlockPos destFloor = destFeet.below();

        // Both intermediate blocks must be passable (to avoid corner-cutting)
        BlockPos interX = new BlockPos(src.x + dx, src.y, src.z);
        BlockPos interZ = new BlockPos(src.x, src.y, src.z + dz);
        BlockPos interXHead = interX.above();
        BlockPos interZHead = interZ.above();

        if (!MovementHelper.canWalkThrough(ctx, destFeet)) return ActionCosts.COST_INF;
        if (!MovementHelper.canWalkThrough(ctx, destHead)) return ActionCosts.COST_INF;
        if (!MovementHelper.canWalkOn(ctx, destFloor)) return ActionCosts.COST_INF;

        // Check that at least one intermediate path is clear (can't clip through corners)
        boolean pathX = MovementHelper.canWalkThrough(ctx, interX) && MovementHelper.canWalkThrough(ctx, interXHead);
        boolean pathZ = MovementHelper.canWalkThrough(ctx, interZ) && MovementHelper.canWalkThrough(ctx, interZHead);
        if (!pathX && !pathZ) return ActionCosts.COST_INF;

        if (MovementHelper.isDanger(ctx, destFeet) || MovementHelper.isDanger(ctx, destFloor)) {
            return ActionCosts.COST_INF;
        }

        return ctx.allowSprint ? ActionCosts.SPRINT_ONE_DIAGONAL_COST : ActionCosts.WALK_ONE_DIAGONAL_COST;
    }

    @Override
    public MovementState.Status tick(ServerPlayer player, ServerLevel level) {
        state.incrementTick();

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
