package com.ultra.megamod.feature.citizen.entity.navigation;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathFinder;

/**
 * Custom path navigation for citizen entities.
 * Allows citizens to open doors and climb ladders while pathfinding.
 * Uses CitizenNodeEvaluator for enhanced walkability near doors and trapdoors.
 */
public class CitizenPathNavigation extends GroundPathNavigation {

    private static final int MAX_VISITED_NODES = 256;

    public CitizenPathNavigation(Mob mob, Level level) {
        super(mob, level);
        this.setCanOpenDoors(true);
        this.setCanFloat(true);
    }

    @Override
    protected PathFinder createPathFinder(int maxVisitedNodes) {
        this.nodeEvaluator = new CitizenNodeEvaluator();
        this.nodeEvaluator.setCanPassDoors(true);
        this.nodeEvaluator.setCanOpenDoors(true);
        this.nodeEvaluator.setCanFloat(true);
        return new PathFinder(this.nodeEvaluator, maxVisitedNodes);
    }

    // canOpenDoors() is on NodeEvaluator, not on PathNavigation.
    // Door opening is configured via setCanOpenDoors(true) in createPathFinder.
}
