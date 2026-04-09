package com.ultra.megamod.feature.baritone.movement.movements;

import com.ultra.megamod.feature.baritone.movement.*;
import com.ultra.megamod.feature.baritone.pathfinding.BetterBlockPos;
import com.ultra.megamod.feature.baritone.pathfinding.CalculationContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 * Free fall down multiple blocks (straight down from src).
 * Only used when there's a safe landing (solid block or water).
 */
public class MovementFall extends Movement {
    private final int fallHeight;

    public MovementFall(CalculationContext ctx, BetterBlockPos src, BetterBlockPos dest) {
        super(src, dest);
        this.fallHeight = src.y - dest.y;
    }

    @Override
    public double calculateCost(CalculationContext ctx) {
        if (fallHeight <= 0) return ActionCosts.COST_INF;

        // Check all blocks in the fall path are clear
        for (int dy = 0; dy < fallHeight; dy++) {
            BlockPos pos = new BlockPos(dest.x, src.y - dy, dest.z);
            if (!MovementHelper.canWalkThrough(ctx, pos)) {
                return ActionCosts.COST_INF;
            }
        }

        // Must have safe landing
        BlockPos landingFloor = new BlockPos(dest.x, dest.y - 1, dest.z);
        boolean water = MovementHelper.isWater(ctx, new BlockPos(dest.x, dest.y, dest.z));
        if (!water && !MovementHelper.canWalkOn(ctx, landingFloor)) {
            return ActionCosts.COST_INF;
        }

        if (MovementHelper.isDanger(ctx, landingFloor)) {
            return ActionCosts.COST_INF;
        }

        double cost = fallHeight * ActionCosts.FALL_ONE_BLOCK_COST;
        if (fallHeight > ActionCosts.MAX_SAFE_FALL && !water) {
            cost += (fallHeight - ActionCosts.MAX_SAFE_FALL) * ActionCosts.FALL_DAMAGE_PENALTY;
        }
        if (fallHeight > ctx.maxFallHeight) {
            return ActionCosts.COST_INF;
        }

        return cost;
    }

    @Override
    public MovementState.Status tick(ServerPlayer player, ServerLevel level) {
        state.incrementTick();

        if (isAtPosition(player, dest)) {
            return MovementState.Status.SUCCESS;
        }

        // Just move towards the dest XZ and let gravity handle Y
        moveTowards(player, centerX(), player.getY(), centerZ(), 0.15);

        if (state.getTicksInCurrent() > 100) {
            return MovementState.Status.FAILED;
        }
        return MovementState.Status.RUNNING;
    }
}
