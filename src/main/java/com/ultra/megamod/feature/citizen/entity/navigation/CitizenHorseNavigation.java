package com.ultra.megamod.feature.citizen.entity.navigation;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;

/**
 * Custom navigation for mounted horseman recruits.
 * Uses a wider path width to account for the horse's size,
 * and does not attempt to open doors (horses can't fit through).
 */
public class CitizenHorseNavigation extends GroundPathNavigation {

    private static final int MAX_VISITED_NODES = 256;

    public CitizenHorseNavigation(Mob mob, Level level) {
        super(mob, level);
        this.setCanFloat(true);
        // Horses cannot open doors
        this.setCanOpenDoors(false);
    }

    @Override
    protected PathFinder createPathFinder(int maxVisitedNodes) {
        this.nodeEvaluator = new WalkNodeEvaluator();
        this.nodeEvaluator.setCanPassDoors(false);
        this.nodeEvaluator.setCanOpenDoors(false);
        this.nodeEvaluator.setCanFloat(true);
        return new PathFinder(this.nodeEvaluator, maxVisitedNodes);
    }

    // Note: In 1.21.11, GroundPathNavigation no longer exposes canMoveDirectly(6 doubles),
    // getSizeX(), or getSizeY() as overridable methods. The node evaluator handles entity
    // dimensions automatically based on the mob's bounding box.
}
