package com.ultra.megamod.feature.citizen.entity.navigation;

import com.ultra.megamod.feature.citizen.colonyblocks.BlockWaypoint;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.PathfindingContext;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;

/**
 * Custom node evaluator for citizen pathfinding.
 * Extends WalkNodeEvaluator to treat doors and trapdoors as walkable/passable,
 * allowing citizens to navigate through buildings with doors and use trapdoors
 * as part of their path.
 */
public class CitizenNodeEvaluator extends WalkNodeEvaluator {

    @Override
    public PathType getPathTypeOfMob(PathfindingContext context, int x, int y, int z, Mob mob) {
        PathType baseType = super.getPathTypeOfMob(context, x, y, z, mob);

        // Treat closed wooden doors as walkable doors (citizens can open them)
        if (baseType == PathType.DOOR_WOOD_CLOSED) {
            return PathType.WALKABLE_DOOR;
        }

        // Check for fence gates, trapdoors, and waypoints by block state
        BlockState state = context.getBlockState(new BlockPos(x, y, z));

        if (state.getBlock() instanceof FenceGateBlock) {
            return PathType.WALKABLE_DOOR;
        }

        // Waypoint blocks are pathfinding markers -- always walkable
        if (state.getBlock() instanceof BlockWaypoint) {
            return PathType.WALKABLE;
        }

        // Treat trapdoors as walkable -- citizens can step on closed trapdoors
        // and navigate through open ones
        if (state.getBlock() instanceof TrapDoorBlock) {
            boolean open = state.getValue(TrapDoorBlock.OPEN);
            if (!open) {
                // Closed trapdoor = solid surface to walk on
                return PathType.WALKABLE;
            } else {
                // Open trapdoor = passable like a door
                return PathType.DOOR_OPEN;
            }
        }

        return baseType;
    }
}
