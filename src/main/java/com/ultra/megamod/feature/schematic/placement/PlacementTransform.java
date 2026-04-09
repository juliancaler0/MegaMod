package com.ultra.megamod.feature.schematic.placement;

import com.ultra.megamod.feature.schematic.data.SchematicData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;

/**
 * Utilities for transforming schematic blocks with rotation and mirror.
 */
public class PlacementTransform {

    /**
     * Transforms a relative position within a schematic by rotation and mirror.
     * The schematic's size is needed to correctly pivot the rotation.
     */
    public static BlockPos transformRelative(BlockPos relPos, Vec3i size, Rotation rotation, Mirror mirror) {
        int x = relPos.getX();
        int y = relPos.getY();
        int z = relPos.getZ();

        // Apply rotation first
        int rx, rz;
        switch (rotation) {
            case CLOCKWISE_90 -> {
                rx = size.getZ() - 1 - z;
                rz = x;
            }
            case CLOCKWISE_180 -> {
                rx = size.getX() - 1 - x;
                rz = size.getZ() - 1 - z;
            }
            case COUNTERCLOCKWISE_90 -> {
                rx = z;
                rz = size.getX() - 1 - x;
            }
            default -> {
                rx = x;
                rz = z;
            }
        }

        // Apply mirror after rotation
        switch (mirror) {
            case FRONT_BACK -> {
                Vec3i tSize = getTransformedSize(size, rotation);
                rx = tSize.getX() - 1 - rx;
            }
            case LEFT_RIGHT -> {
                Vec3i tSize = getTransformedSize(size, rotation);
                rz = tSize.getZ() - 1 - rz;
            }
            default -> {}
        }

        return new BlockPos(rx, y, rz);
    }

    /**
     * Transforms a BlockState's directional properties by rotation and mirror.
     * Uses Minecraft's built-in rotate/mirror methods.
     */
    public static BlockState transformBlockState(BlockState state, Rotation rotation, Mirror mirror) {
        if (rotation != Rotation.NONE) {
            state = state.rotate(rotation);
        }
        if (mirror != Mirror.NONE) {
            state = state.mirror(mirror);
        }
        return state;
    }

    /**
     * Returns all schematic blocks resolved to absolute world positions with transforms applied.
     */
    public static Map<BlockPos, BlockState> getWorldBlocks(SchematicPlacement placement) {
        SchematicData schematic = placement.getSchematic();
        BlockPos origin = placement.getOrigin();
        Rotation rotation = placement.getRotation();
        Mirror mirror = placement.getMirror();
        Vec3i size = schematic.getSize();

        Map<BlockPos, BlockState> worldBlocks = new HashMap<>(schematic.getBlocks().size());

        for (Map.Entry<BlockPos, BlockState> entry : schematic.getBlocks().entrySet()) {
            BlockPos relPos = entry.getKey();
            BlockState state = entry.getValue();

            BlockPos transformed = transformRelative(relPos, size, rotation, mirror);
            BlockState transformedState = transformBlockState(state, rotation, mirror);

            BlockPos worldPos = origin.offset(transformed);
            worldBlocks.put(worldPos, transformedState);
        }

        return worldBlocks;
    }

    /**
     * Returns the bounding box size after rotation.
     */
    public static Vec3i getTransformedSize(Vec3i size, Rotation rotation) {
        return switch (rotation) {
            case CLOCKWISE_90, COUNTERCLOCKWISE_90 -> new Vec3i(size.getZ(), size.getY(), size.getX());
            default -> size;
        };
    }
}
