package com.ultra.megamod.feature.baritone.movement.movements;

import com.ultra.megamod.feature.baritone.movement.*;
import com.ultra.megamod.feature.baritone.pathfinding.BetterBlockPos;
import com.ultra.megamod.feature.baritone.pathfinding.CalculationContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 * Horizontal swimming movement through water (1 block in X or Z).
 */
public class MovementSwim extends Movement {
    public MovementSwim(CalculationContext ctx, BetterBlockPos src, BetterBlockPos dest) {
        super(src, dest);
    }

    @Override
    public double calculateCost(CalculationContext ctx) {
        if (!ctx.settings.allowSwim) return ActionCosts.COST_INF;

        BlockPos srcFeet = new BlockPos(src.x, src.y, src.z);
        BlockPos destFeet = new BlockPos(dest.x, dest.y, dest.z);

        // Source must be in water
        if (!MovementHelper.isWater(ctx, srcFeet)) {
            return ActionCosts.COST_INF;
        }

        // Dest must be water or passable (exiting water)
        boolean destInWater = MovementHelper.isWater(ctx, destFeet);
        if (!destInWater && !MovementHelper.canWalkThrough(ctx, destFeet)) {
            return ActionCosts.COST_INF;
        }

        // If exiting water, need floor under dest
        if (!destInWater) {
            BlockPos destFloor = destFeet.below();
            if (!MovementHelper.canWalkOn(ctx, destFloor)) {
                return ActionCosts.COST_INF;
            }
        }

        // Danger check
        if (MovementHelper.isDanger(ctx, destFeet)) {
            return ActionCosts.COST_INF;
        }

        return ActionCosts.SWIM_ONE_BLOCK_COST;
    }

    @Override
    public MovementState.Status tick(ServerPlayer player, ServerLevel level) {
        state.incrementTick();

        if (isAtPosition(player, dest)) {
            return MovementState.Status.SUCCESS;
        }

        // Swim towards destination — slightly upward to stay afloat
        double targetY = dest.y;
        if (player.isInWater()) {
            targetY = Math.max(targetY, player.getY() + 0.04); // Gentle float up
        }
        moveTowards(player, centerX(), targetY, centerZ(), 0.15);

        if (state.getTicksInCurrent() > 80) {
            return MovementState.Status.FAILED;
        }
        return MovementState.Status.RUNNING;
    }
}
