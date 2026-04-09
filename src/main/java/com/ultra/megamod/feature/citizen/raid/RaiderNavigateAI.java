package com.ultra.megamod.feature.citizen.raid;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.EnumSet;

/**
 * Navigation goal that paths raiders toward their colony target position.
 * Can optionally break doors. Drowned pirates do not avoid water.
 */
public class RaiderNavigateAI extends Goal {

    private final AbstractRaiderEntity raider;
    private final double speed;
    private final boolean breakDoors;
    private int pathRecalcTimer;
    private int doorBreakTimer;
    private BlockPos currentDoor;

    public RaiderNavigateAI(AbstractRaiderEntity raider, double speed, boolean breakDoors) {
        this.raider = raider;
        this.speed = speed;
        this.breakDoors = breakDoors;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        // Only navigate if we have a target position set and no attack target
        return raider.getTarget() == null
                && !raider.getTargetPos().equals(BlockPos.ZERO);
    }

    @Override
    public boolean canContinueToUse() {
        return canUse() && !raider.getNavigation().isDone();
    }

    @Override
    public void start() {
        pathRecalcTimer = 0;
        doorBreakTimer = 0;
        currentDoor = null;
        navigateToTarget();
    }

    @Override
    public void stop() {
        raider.getNavigation().stop();
        currentDoor = null;
    }

    @Override
    public void tick() {
        if (--pathRecalcTimer <= 0) {
            pathRecalcTimer = 20; // Recalculate path every second
            navigateToTarget();
        }

        // Door breaking logic
        if (breakDoors) {
            tryBreakDoor();
        }
    }

    private void navigateToTarget() {
        BlockPos target = raider.getTargetPos();
        raider.getNavigation().moveTo(
                target.getX() + 0.5,
                target.getY(),
                target.getZ() + 0.5,
                speed
        );
    }

    private void tryBreakDoor() {
        BlockPos feetPos = raider.blockPosition();
        // Check blocks in front at feet and head height
        for (int dy = 0; dy <= 1; dy++) {
            for (BlockPos check : new BlockPos[]{
                    feetPos.north().above(dy),
                    feetPos.south().above(dy),
                    feetPos.east().above(dy),
                    feetPos.west().above(dy)
            }) {
                BlockState state = raider.level().getBlockState(check);
                if (state.getBlock() instanceof DoorBlock) {
                    if (currentDoor == null || !currentDoor.equals(check)) {
                        currentDoor = check;
                        doorBreakTimer = 0;
                    }
                    doorBreakTimer++;
                    // Break door after ~3 seconds of bashing (60 ticks)
                    raider.level().destroyBlockProgress(raider.getId(), check,
                            (int) ((doorBreakTimer / 60.0f) * 10));
                    if (doorBreakTimer >= 60) {
                        raider.level().destroyBlock(check, true);
                        currentDoor = null;
                        doorBreakTimer = 0;
                    }
                    return;
                }
            }
        }
        // No door nearby, reset
        if (currentDoor != null) {
            raider.level().destroyBlockProgress(raider.getId(), currentDoor, -1);
            currentDoor = null;
            doorBreakTimer = 0;
        }
    }
}
