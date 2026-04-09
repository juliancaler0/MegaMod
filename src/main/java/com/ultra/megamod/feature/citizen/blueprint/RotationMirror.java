package com.ultra.megamod.feature.citizen.blueprint;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.Vec3;

/**
 * Represents all 8 possible states of a structure when rotating and/or mirroring.
 * Combines 4 rotations (NONE, CW90, CW180, CCW90) with 2 mirror states (NONE, FRONT_BACK).
 * <p>
 * Lookup tables are initialized in the static block to support efficient chaining
 * of rotation and mirror operations without branching logic.
 */
public enum RotationMirror {
    NONE(Rotation.NONE, Mirror.NONE),
    R90(Rotation.CLOCKWISE_90, Mirror.NONE),
    R180(Rotation.CLOCKWISE_180, Mirror.NONE),
    R270(Rotation.COUNTERCLOCKWISE_90, Mirror.NONE),
    MIR_NONE(Rotation.NONE, Mirror.FRONT_BACK),
    MIR_R90(Rotation.CLOCKWISE_90, Mirror.FRONT_BACK),
    MIR_R180(Rotation.CLOCKWISE_180, Mirror.FRONT_BACK),
    MIR_R270(Rotation.COUNTERCLOCKWISE_90, Mirror.FRONT_BACK);

    /** All mirrored variants. */
    public static final RotationMirror[] MIRRORED = {MIR_NONE, MIR_R90, MIR_R180, MIR_R270};

    /** All non-mirrored variants. */
    public static final RotationMirror[] NOT_MIRRORED = {NONE, R90, R180, R270};

    // Lookup tables for chaining — initialized in static block because enum constants
    // cannot reference each other during construction.
    private RotationMirror rotateCW90;
    private RotationMirror rotateCW180;
    private RotationMirror rotateCCW90;
    private RotationMirror mirrorFB;
    private RotationMirror mirrorLR;

    static {
        // Clockwise 90
        NONE.rotateCW90 = R90;
        R90.rotateCW90 = R180;
        R180.rotateCW90 = R270;
        R270.rotateCW90 = NONE;
        MIR_NONE.rotateCW90 = MIR_R90;
        MIR_R90.rotateCW90 = MIR_R180;
        MIR_R180.rotateCW90 = MIR_R270;
        MIR_R270.rotateCW90 = MIR_NONE;

        // Clockwise 180
        NONE.rotateCW180 = R180;
        R90.rotateCW180 = R270;
        R180.rotateCW180 = NONE;
        R270.rotateCW180 = R90;
        MIR_NONE.rotateCW180 = MIR_R180;
        MIR_R90.rotateCW180 = MIR_R270;
        MIR_R180.rotateCW180 = MIR_NONE;
        MIR_R270.rotateCW180 = MIR_R90;

        // Counter-clockwise 90
        NONE.rotateCCW90 = R270;
        R90.rotateCCW90 = NONE;
        R180.rotateCCW90 = R90;
        R270.rotateCCW90 = R180;
        MIR_NONE.rotateCCW90 = MIR_R270;
        MIR_R90.rotateCCW90 = MIR_NONE;
        MIR_R180.rotateCCW90 = MIR_R90;
        MIR_R270.rotateCCW90 = MIR_R180;

        // Mirror FRONT_BACK
        NONE.mirrorFB = MIR_NONE;
        R90.mirrorFB = MIR_R270;
        R180.mirrorFB = MIR_R180;
        R270.mirrorFB = MIR_R90;
        MIR_NONE.mirrorFB = NONE;
        MIR_R90.mirrorFB = R270;
        MIR_R180.mirrorFB = R180;
        MIR_R270.mirrorFB = R90;

        // Mirror LEFT_RIGHT
        NONE.mirrorLR = MIR_R180;
        R90.mirrorLR = MIR_R90;
        R180.mirrorLR = MIR_NONE;
        R270.mirrorLR = MIR_R270;
        MIR_NONE.mirrorLR = R180;
        MIR_R90.mirrorLR = R90;
        MIR_R180.mirrorLR = NONE;
        MIR_R270.mirrorLR = R270;
    }

    private final Rotation rotation;
    private final Mirror mirror;

    RotationMirror(Rotation rotation, Mirror mirror) {
        this.rotation = rotation;
        this.mirror = mirror;
    }

    /** Returns the Minecraft {@link Rotation} component. */
    public Rotation getRotation() {
        return rotation;
    }

    /** Returns the Minecraft {@link Mirror} component. */
    public Mirror getMirror() {
        return mirror;
    }

    /** Returns true if this state includes mirroring. */
    public boolean isMirrored() {
        return mirror != Mirror.NONE;
    }

    /**
     * Applies an additional rotation to the current state.
     *
     * @param offset the rotation to add
     * @return the resulting RotationMirror
     */
    public RotationMirror rotate(Rotation offset) {
        return switch (offset) {
            case CLOCKWISE_90 -> rotateCW90;
            case CLOCKWISE_180 -> rotateCW180;
            case COUNTERCLOCKWISE_90 -> rotateCCW90;
            case NONE -> this;
        };
    }

    /**
     * Flips the mirror state (NONE becomes FRONT_BACK, FRONT_BACK becomes NONE).
     *
     * @return the mirrored RotationMirror
     */
    public RotationMirror mirror() {
        return mirrorFB;
    }

    /**
     * Applies the specified mirror to the current state.
     *
     * @param mirrorIn the mirror to apply
     * @return the resulting RotationMirror
     */
    public RotationMirror mirror(Mirror mirrorIn) {
        return switch (mirrorIn) {
            case LEFT_RIGHT -> mirrorLR;
            case FRONT_BACK -> mirrorFB;
            case NONE -> this;
        };
    }

    /**
     * Creates a RotationMirror from the given Rotation and Mirror combination.
     */
    public static RotationMirror of(Rotation rotation, Mirror mirror) {
        return NONE.mirror(mirror).rotate(rotation);
    }

    /**
     * Transforms a BlockPos using this rotation/mirror around the zero pivot.
     *
     * @param pos the position to transform
     * @return the transformed position
     */
    public BlockPos applyToPos(BlockPos pos) {
        return applyToPos(pos, BlockPos.ZERO);
    }

    /**
     * Transforms a BlockPos using this rotation/mirror around the given pivot.
     *
     * @param pos   the position to transform
     * @param pivot the center of the transformation
     * @return the transformed position
     */
    public BlockPos applyToPos(BlockPos pos, BlockPos pivot) {
        return StructureTemplate.transform(pos, mirror, rotation, pivot);
    }

    /**
     * Transforms a Vec3 using this rotation/mirror around the zero pivot.
     *
     * @param pos the position to transform
     * @return the transformed position
     */
    public Vec3 applyToPos(Vec3 pos) {
        return applyToPos(pos, BlockPos.ZERO);
    }

    /**
     * Transforms a Vec3 using this rotation/mirror around the given pivot.
     *
     * @param pos   the position to transform
     * @param pivot the center of the transformation
     * @return the transformed position
     */
    public Vec3 applyToPos(Vec3 pos, BlockPos pivot) {
        return StructureTemplate.transform(pos, mirror, rotation, pivot);
    }

    /**
     * Computes the RotationMirror that, when added to this, produces the target end state.
     * Formula: this + result = end, so result = end - this.
     *
     * @param end the desired end state
     * @return the difference RotationMirror
     */
    public RotationMirror calcDifferenceTowards(RotationMirror end) {
        RotationMirror[] candidates = (this.mirror == end.mirror) ? NOT_MIRRORED : MIRRORED;
        for (RotationMirror candidate : candidates) {
            if (add(candidate) == end) {
                return candidate;
            }
        }
        throw new IllegalStateException("Missing RotationMirror path from " + this + " to " + end);
    }

    /**
     * Combines this RotationMirror with another additively.
     * Formula: this + other = result.
     *
     * @param other the RotationMirror to add
     * @return the combined result
     */
    public RotationMirror add(RotationMirror other) {
        return this.mirror(other.mirror).rotate(other.rotation);
    }
}
