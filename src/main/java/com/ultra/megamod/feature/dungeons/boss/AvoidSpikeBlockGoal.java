package com.ultra.megamod.feature.dungeons.boss;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * AI goal that makes dungeon bosses avoid ledges with drops greater than 2 blocks.
 * Spikes are placed at the bottom of pits, so avoiding long drops keeps bosses
 * from falling onto them. Scans a 2-block radius for edges and steers away.
 */
public class AvoidSpikeBlockGoal extends Goal {
    private final PathfinderMob mob;
    private Vec3 safeTarget;
    private int ticksActive;
    private static final int MAX_ACTIVE_TICKS = 15;

    public AvoidSpikeBlockGoal(PathfinderMob mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        // Flying mobs don't need ledge avoidance
        if (mob.isNoGravity()) return false;
        // Don't avoid ledges when actively fighting — chase the player across bridges
        if (mob.getTarget() != null && mob.getTarget().isAlive()) return false;

        BlockPos ledge = findNearbyLedge(2);
        if (ledge == null) return false;

        Vec3 ledgeCenter = Vec3.atCenterOf(ledge);
        Vec3 awayDir = mob.position().subtract(ledgeCenter);
        if (awayDir.horizontalDistanceSqr() < 0.01) {
            double angle = mob.getRandom().nextDouble() * Math.PI * 2;
            awayDir = new Vec3(Math.cos(angle), 0, Math.sin(angle));
        }
        awayDir = new Vec3(awayDir.x, 0, awayDir.z).normalize();

        this.safeTarget = mob.position().add(awayDir.x * 3.0, 0, awayDir.z * 3.0);
        return true;
    }

    @Override
    public void start() {
        this.ticksActive = 0;
        mob.getNavigation().moveTo(safeTarget.x, safeTarget.y, safeTarget.z, 1.3);
    }

    @Override
    public boolean canContinueToUse() {
        this.ticksActive++;
        // Time-box the avoidance so combat goals can re-engage
        if (this.ticksActive >= MAX_ACTIVE_TICKS) return false;
        return !mob.getNavigation().isDone() && findNearbyLedge(1) != null;
    }

    @Override
    public void stop() {
        mob.getNavigation().stop();
    }

    /**
     * Find the nearest position within radius that has a drop >2 blocks.
     */
    private BlockPos findNearbyLedge(int radius) {
        Level level = mob.level();
        BlockPos mobPos = mob.blockPosition();
        BlockPos nearest = null;
        double nearestDistSq = Double.MAX_VALUE;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                BlockPos checkPos = mobPos.offset(dx, 0, dz);
                if (isDangerousDrop(level, checkPos)) {
                    double distSq = checkPos.distSqr(mobPos);
                    if (distSq < nearestDistSq) {
                        nearestDistSq = distSq;
                        nearest = checkPos;
                    }
                }
            }
        }
        return nearest;
    }

    /**
     * Returns true if stepping to this position would cause a fall >2 blocks.
     * Checks for solid ground within 3 blocks below the feet position.
     * Iron bars and similar partial blocks also count as safe ground.
     */
    static boolean isDangerousDrop(Level level, BlockPos feetPos) {
        for (int dy = -1; dy >= -3; dy--) {
            BlockState state = level.getBlockState(feetPos.offset(0, dy, 0));
            if (state.isSolid() || state.getBlock() instanceof IronBarsBlock) {
                return false;
            }
        }
        return true;
    }
}
