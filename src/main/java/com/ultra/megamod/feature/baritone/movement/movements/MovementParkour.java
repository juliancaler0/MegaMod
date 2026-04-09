package com.ultra.megamod.feature.baritone.movement.movements;

import com.ultra.megamod.feature.baritone.movement.*;
import com.ultra.megamod.feature.baritone.pathfinding.BetterBlockPos;
import com.ultra.megamod.feature.baritone.pathfinding.CalculationContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 * Sprint jump across a 1-block gap (2 blocks horizontal, same Y).
 */
public class MovementParkour extends Movement {
    private final int dx, dz;
    private boolean jumped = false;

    public MovementParkour(CalculationContext ctx, BetterBlockPos src, int dx, int dz) {
        super(src, new BetterBlockPos(src.x + dx * 2, src.y, src.z + dz * 2));
        this.dx = dx;
        this.dz = dz;
    }

    @Override
    public double calculateCost(CalculationContext ctx) {
        if (!ctx.allowParkour || !ctx.allowSprint) return ActionCosts.COST_INF;

        // Gap block (1 away) must be clear (air — the gap)
        BlockPos gapFeet = new BlockPos(src.x + dx, src.y, src.z + dz);
        BlockPos gapHead = gapFeet.above();

        // Landing block (2 away)
        BlockPos landFeet = new BlockPos(dest.x, dest.y, dest.z);
        BlockPos landHead = landFeet.above();
        BlockPos landFloor = landFeet.below();

        // Headroom along entire path
        BlockPos srcAboveHead = new BlockPos(src.x, src.y + 2, src.z);
        if (!MovementHelper.canWalkThrough(ctx, srcAboveHead)) return ActionCosts.COST_INF;
        if (!MovementHelper.canWalkThrough(ctx, gapFeet)) return ActionCosts.COST_INF;
        if (!MovementHelper.canWalkThrough(ctx, gapHead)) return ActionCosts.COST_INF;
        BlockPos gapAboveHead = new BlockPos(src.x + dx, src.y + 2, src.z + dz);
        if (!MovementHelper.canWalkThrough(ctx, gapAboveHead)) return ActionCosts.COST_INF;

        // Landing must be solid
        if (!MovementHelper.canWalkThrough(ctx, landFeet)) return ActionCosts.COST_INF;
        if (!MovementHelper.canWalkThrough(ctx, landHead)) return ActionCosts.COST_INF;
        if (!MovementHelper.canWalkOn(ctx, landFloor)) return ActionCosts.COST_INF;

        if (MovementHelper.isDanger(ctx, landFeet) || MovementHelper.isDanger(ctx, landFloor)) {
            return ActionCosts.COST_INF;
        }

        return ActionCosts.SPRINT_ONE_BLOCK_COST * 2 + ActionCosts.JUMP_PENALTY;
    }

    @Override
    public MovementState.Status tick(ServerPlayer player, ServerLevel level) {
        state.incrementTick();

        if (isAtPosition(player, dest)) {
            jumped = false; // Reset for potential reuse
            return MovementState.Status.SUCCESS;
        }

        // Sprint-speed teleport towards destination
        moveTowards(player, centerX(), centerY(), centerZ(), 0.28);

        // Jump at the edge — reset jumped flag when movement starts fresh
        if (state.getTicksInCurrent() <= 1) {
            jumped = false;
        }
        if (!jumped && player.onGround() && state.getTicksInCurrent() >= 2) {
            jump(player);
            jumped = true;
        }

        if (state.getTicksInCurrent() > 40) {
            return MovementState.Status.FAILED;
        }
        return MovementState.Status.RUNNING;
    }
}
