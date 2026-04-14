package com.ultra.megamod.lib.emf.jem;

import java.util.Arrays;

/**
 * One cuboid inside a model part. Mirrors the OptiFine
 * <a href="https://github.com/sp614x/optifine/blob/master/OptiFineDoc/doc/cem_part.txt">
 * cem_part.txt</a> spec.
 * <p>
 * The JSON fields are kept in their upstream names (snake_case / camelCase mix) so
 * Gson deserialisation works verbatim on real {@code .jem} / {@code .jpm} files.
 * Ported 1:1 from upstream.
 */
@SuppressWarnings("CanBeFinal")
public class EmfBoxData {

    public float[] textureOffset = {};
    public float[] uvDown = {};
    public float[] uvUp = {};
    public float[] uvFront = {};
    public float[] uvBack = {};
    public float[] uvLeft = {};
    public float[] uvRight = {};
    public float[] uvNorth = {};
    public float[] uvSouth = {};
    public float[] uvWest = {};
    public float[] uvEast = {};

    public float[] coordinates = {};
    /** Dilation applied uniformly to the box size. */
    public float sizeAdd = 0.0f;

    /** EMF-only: per-axis dilation. {@code sizeAdd} is used when these are zero. */
    public float sizeAddX = 0.0f;
    public float sizeAddY = 0.0f;
    public float sizeAddZ = 0.0f;

    /** EMF-only shorthand: sets {@code sizeAddX/Y/Z} when length is 3. */
    public float[] sizesAdd = {};

    /** Normalise legacy / shorthand fields. Must be called once after deserialization. */
    public void prepare(boolean invertX, boolean invertY, boolean invertZ) {
        try {
            if (sizeAdd != 0.0f && sizeAddX == 0.0f && sizeAddY == 0.0f && sizeAddZ == 0.0f) {
                sizeAddX = sizeAdd;
                sizeAddY = sizeAdd;
                sizeAddZ = sizeAdd;
            }
            if (sizesAdd.length == 3) {
                sizeAddX = sizesAdd[0];
                sizeAddY = sizesAdd[1];
                sizeAddZ = sizesAdd[2];
            }

            // axis inversion
            if (coordinates.length == 6) {
                if (invertX) coordinates[0] = -coordinates[0] - coordinates[3];
                if (invertY) coordinates[1] = -coordinates[1] - coordinates[4];
                if (invertZ) coordinates[2] = -coordinates[2] - coordinates[5];
            }

            boolean offsetValid = textureOffset.length == 2;
            if (!offsetValid && textureOffset.length != 0) {
                throw new IllegalArgumentException("Invalid textureOffset data: " + Arrays.toString(textureOffset));
            }

            if (!offsetValid) {
                checkAndFixUVLegacyDirections();
                validateUV(uvDown, "uvDown");
                validateUV(uvUp, "uvUp");
                validateUV(uvNorth, "uvNorth");
                validateUV(uvSouth, "uvSouth");
                validateUV(uvWest, "uvWest");
                validateUV(uvEast, "uvEast");
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Error preparing box data: " + e.getMessage(), e);
        }
    }

    private void validateUV(float[] uv, String name) {
        if (uv.length == 0) return;
        if (uv.length != 4) {
            throw new IllegalArgumentException(
                    "Invalid UV data for [" + name + "], must have 4 or 0 values: " + Arrays.toString(uv));
        }
    }

    /** Legacy direction aliases ({@code uvFront}, {@code uvBack}, {@code uvLeft}, {@code uvRight}). */
    public void checkAndFixUVLegacyDirections() {
        if (uvFront.length == 4) uvNorth = uvFront;
        if (uvBack.length == 4) uvWest = uvBack;
        if (uvLeft.length == 4) uvNorth = uvLeft;
        if (uvRight.length == 4) uvEast = uvRight;
    }

    @Override
    public String toString() {
        return "EmfBoxData{coords=" + Arrays.toString(coordinates)
                + ", sizeAdd=" + sizeAdd + '}';
    }
}
