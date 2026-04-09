package com.ultra.megamod.feature.citizen.blueprint;

import net.minecraft.core.BlockPos;

/**
 * Configuration for placing a blueprint in the world.
 * Holds position, rotation/mirror, and ground-level behavior.
 */
public class PlacementSettings {

    /**
     * Controls how the blueprint aligns with the ground.
     */
    public enum GroundStyle {
        /** No ground adjustment; place exactly at the specified Y. */
        NONE,
        /** Adjust placement relative to the terrain surface. */
        RELATIVE
    }

    private RotationMirror rotationMirror;
    private BlockPos position;
    private GroundStyle groundStyle;

    /**
     * Creates placement settings with default values (no rotation, origin, no ground adjustment).
     */
    public PlacementSettings() {
        this.rotationMirror = RotationMirror.NONE;
        this.position = BlockPos.ZERO;
        this.groundStyle = GroundStyle.NONE;
    }

    /**
     * Creates placement settings with all values specified.
     *
     * @param rotationMirror the rotation and mirror state
     * @param position       the world position for placement
     * @param groundStyle    the ground alignment style
     */
    public PlacementSettings(RotationMirror rotationMirror, BlockPos position, GroundStyle groundStyle) {
        this.rotationMirror = rotationMirror;
        this.position = position;
        this.groundStyle = groundStyle;
    }

    public RotationMirror getRotationMirror() {
        return rotationMirror;
    }

    public PlacementSettings setRotationMirror(RotationMirror rotationMirror) {
        this.rotationMirror = rotationMirror;
        return this;
    }

    public BlockPos getPosition() {
        return position;
    }

    public PlacementSettings setPosition(BlockPos position) {
        this.position = position;
        return this;
    }

    public GroundStyle getGroundStyle() {
        return groundStyle;
    }

    public PlacementSettings setGroundStyle(GroundStyle groundStyle) {
        this.groundStyle = groundStyle;
        return this;
    }

    @Override
    public String toString() {
        return "PlacementSettings[rotMir=" + rotationMirror +
                ", pos=" + position +
                ", ground=" + groundStyle + "]";
    }
}
