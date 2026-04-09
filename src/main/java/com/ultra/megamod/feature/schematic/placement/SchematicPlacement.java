package com.ultra.megamod.feature.schematic.placement;

import com.ultra.megamod.feature.schematic.data.SchematicData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;
import java.util.UUID;

/**
 * Represents a schematic placed in the world with position, rotation, and mirror.
 */
public class SchematicPlacement {

    private final UUID id;
    private final SchematicData schematic;
    private BlockPos origin;
    private Rotation rotation;
    private Mirror mirror;
    private boolean locked;
    private String name;

    public SchematicPlacement(SchematicData schematic, BlockPos origin) {
        this.id = UUID.randomUUID();
        this.schematic = schematic;
        this.origin = origin;
        this.rotation = Rotation.NONE;
        this.mirror = Mirror.NONE;
        this.locked = false;
        this.name = schematic.getName();
    }

    public UUID getId() { return id; }
    public SchematicData getSchematic() { return schematic; }

    public BlockPos getOrigin() { return origin; }
    public void setOrigin(BlockPos origin) {
        if (!locked) this.origin = origin;
    }

    public Rotation getRotation() { return rotation; }
    public void setRotation(Rotation rotation) {
        if (!locked) this.rotation = rotation;
    }

    public Mirror getMirror() { return mirror; }
    public void setMirror(Mirror mirror) {
        if (!locked) this.mirror = mirror;
    }

    public boolean isLocked() { return locked; }
    public void setLocked(boolean locked) { this.locked = locked; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    /**
     * Cycles to the next rotation value.
     */
    public void cycleRotation() {
        if (locked) return;
        this.rotation = switch (rotation) {
            case NONE -> Rotation.CLOCKWISE_90;
            case CLOCKWISE_90 -> Rotation.CLOCKWISE_180;
            case CLOCKWISE_180 -> Rotation.COUNTERCLOCKWISE_90;
            case COUNTERCLOCKWISE_90 -> Rotation.NONE;
        };
    }

    /**
     * Cycles to the next mirror value.
     */
    public void cycleMirror() {
        if (locked) return;
        this.mirror = switch (mirror) {
            case NONE -> Mirror.FRONT_BACK;
            case FRONT_BACK -> Mirror.LEFT_RIGHT;
            case LEFT_RIGHT -> Mirror.NONE;
        };
    }

    /**
     * Moves the origin by the given offset.
     */
    public void nudge(int dx, int dy, int dz) {
        if (!locked) {
            this.origin = this.origin.offset(dx, dy, dz);
        }
    }

    /**
     * Returns all blocks resolved to world positions with transforms applied.
     */
    public Map<BlockPos, BlockState> getWorldBlocks() {
        return PlacementTransform.getWorldBlocks(this);
    }

    /**
     * Returns the transformed bounding box size.
     */
    public Vec3i getTransformedSize() {
        return PlacementTransform.getTransformedSize(schematic.getSize(), rotation);
    }

    /**
     * Returns the rotation as an integer (0, 1, 2, 3) for serialization.
     */
    public int getRotationIndex() {
        return switch (rotation) {
            case NONE -> 0;
            case CLOCKWISE_90 -> 1;
            case CLOCKWISE_180 -> 2;
            case COUNTERCLOCKWISE_90 -> 3;
        };
    }

    /**
     * Returns the mirror as an integer (0, 1, 2) for serialization.
     */
    public int getMirrorIndex() {
        return switch (mirror) {
            case NONE -> 0;
            case FRONT_BACK -> 1;
            case LEFT_RIGHT -> 2;
        };
    }

    public static Rotation rotationFromIndex(int index) {
        return switch (index) {
            case 1 -> Rotation.CLOCKWISE_90;
            case 2 -> Rotation.CLOCKWISE_180;
            case 3 -> Rotation.COUNTERCLOCKWISE_90;
            default -> Rotation.NONE;
        };
    }

    public static Mirror mirrorFromIndex(int index) {
        return switch (index) {
            case 1 -> Mirror.FRONT_BACK;
            case 2 -> Mirror.LEFT_RIGHT;
            default -> Mirror.NONE;
        };
    }
}
