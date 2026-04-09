package com.ultra.megamod.feature.citizen.entity.navigation;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.SwimNodeEvaluator;

/**
 * Water-based navigation for fisherman citizens using boats.
 * Extends WaterBoundPathNavigation to allow navigation through water bodies.
 * Used when a fisherman is operating a boat or swimming to a fishing spot.
 */
public class SailorNavigation extends WaterBoundPathNavigation {

    public SailorNavigation(Mob mob, Level level) {
        super(mob, level);
    }

    @Override
    protected PathFinder createPathFinder(int maxVisitedNodes) {
        this.nodeEvaluator = new SwimNodeEvaluator(true);
        return new PathFinder(this.nodeEvaluator, maxVisitedNodes);
    }

    @Override
    protected boolean canUpdatePath() {
        return this.mob.isInWater() || this.mob.isPassenger();
    }

    @Override
    public boolean isStableDestination(net.minecraft.core.BlockPos pos) {
        // Water and waterlogged blocks are valid destinations for sailor navigation
        return this.mob.level().getBlockState(pos).liquid()
                || super.isStableDestination(pos);
    }
}
