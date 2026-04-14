package com.ultra.megamod.lib.emf.geometry;

import com.ultra.megamod.lib.emf.jem.EmfBoxData;
import com.ultra.megamod.lib.emf.jem.EmfPartData;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.core.Direction;

import java.util.EnumSet;

/**
 * A {@link ModelPart.Cube} built from an OptiFine {@code .jem} {@link EmfBoxData}.
 * <p>
 * Handles the two cube modes:
 * <ul>
 *   <li>Standard UV — a single {@code textureOffset: [u,v]} origin with derived faces.</li>
 *   <li>Per-face UV — {@code uvNorth/uvSouth/uvEast/uvWest/uvUp/uvDown} arrays of
 *       four floats each, letting the pack author paint each face independently.</li>
 * </ul>
 * Also honours {@code mirrorTexture: "u"/"v"/"uv"} which flips UV layout on the relevant
 * axis, and per-axis dilation via {@code sizeAddX/Y/Z} (also accepts the shorthand
 * {@code sizeAdd}).
 * <p>
 * Polygon ordering matches vanilla's constructor order — DOWN, UP, WEST, NORTH, EAST,
 * SOUTH — so downstream consumers that iterate {@code cube.polygons} don't need to care
 * whether the cube came from vanilla or from an EMF box.
 */
public final class EmfCube extends ModelPart.Cube {

    /** Builds a cube using OptiFine's standard U/V origin (single texture offset). */
    public static EmfCube standard(EmfPartData part, EmfBoxData box) {
        boolean mirrorU = part.mirrorTexture != null && part.mirrorTexture.contains("u");
        boolean mirrorV = part.mirrorTexture != null && part.mirrorTexture.contains("v");
        int texW = part.textureSize != null && part.textureSize.length >= 1 ? part.textureSize[0] : 64;
        int texH = part.textureSize != null && part.textureSize.length >= 2 ? part.textureSize[1] : 32;

        return new EmfCube(
                (int) box.textureOffset[0], (int) box.textureOffset[1],
                box.coordinates[0], box.coordinates[1], box.coordinates[2],
                box.coordinates[3], box.coordinates[4], box.coordinates[5],
                box.sizeAddX, box.sizeAddY, box.sizeAddZ,
                texW, texH,
                mirrorU, mirrorV,
                box, part);
    }

    /** Builds a cube using per-face UV arrays (uvDown/uvUp/uvNorth/uvSouth/uvWest/uvEast). */
    public static EmfCube perFaceUv(EmfPartData part, EmfBoxData box) {
        int texW = part.textureSize != null && part.textureSize.length >= 1 ? part.textureSize[0] : 64;
        int texH = part.textureSize != null && part.textureSize.length >= 2 ? part.textureSize[1] : 32;
        // Upstream EMF notes: OptiFine ignores mirror flags when per-face UVs are provided.
        return new EmfCube(
                box.uvDown, box.uvUp, box.uvNorth, box.uvSouth, box.uvWest, box.uvEast,
                box.coordinates[0], box.coordinates[1], box.coordinates[2],
                box.coordinates[3], box.coordinates[4], box.coordinates[5],
                box.sizeAddX, box.sizeAddY, box.sizeAddZ,
                texW, texH,
                false, false,
                box, part);
    }

    public static EmfCube of(EmfPartData part, EmfBoxData box) {
        if (box.textureOffset != null && box.textureOffset.length == 2) {
            return standard(part, box);
        }
        return perFaceUv(part, box);
    }

    // --- Standard UV constructor ---
    private EmfCube(int textureU, int textureV,
                    float cubeX, float cubeY, float cubeZ,
                    float sizeX, float sizeY, float sizeZ,
                    float extraX, float extraY, float extraZ,
                    float textureWidth, float textureHeight,
                    boolean mirrorU, boolean mirrorV,
                    EmfBoxData box, EmfPartData part) {
        super(textureU, textureV,
                cubeX, cubeY, cubeZ,
                sizeX, sizeY, sizeZ,
                extraX, extraY, extraZ,
                false,
                textureWidth, textureHeight,
                EnumSet.allOf(Direction.class));
        rebuildStandardPolygons(cubeX, cubeY, cubeZ, sizeX, sizeY, sizeZ,
                extraX, extraY, extraZ, textureU, textureV,
                textureWidth, textureHeight, mirrorU, mirrorV);
    }

    // --- Per-face UV constructor ---
    private EmfCube(float[] uvDown, float[] uvUp, float[] uvNorth, float[] uvSouth,
                    float[] uvWest, float[] uvEast,
                    float cubeX, float cubeY, float cubeZ,
                    float sizeX, float sizeY, float sizeZ,
                    float extraX, float extraY, float extraZ,
                    float textureWidth, float textureHeight,
                    boolean mirrorU, boolean mirrorV,
                    EmfBoxData box, EmfPartData part) {
        super(0, 0,
                cubeX, cubeY, cubeZ,
                sizeX, sizeY, sizeZ,
                extraX, extraY, extraZ,
                false,
                textureWidth, textureHeight,
                EnumSet.allOf(Direction.class));
        rebuildPerFacePolygons(cubeX, cubeY, cubeZ, sizeX, sizeY, sizeZ,
                extraX, extraY, extraZ,
                uvDown, uvUp, uvNorth, uvSouth, uvWest, uvEast,
                textureWidth, textureHeight, mirrorU, mirrorV);
    }

    // ---------------------------------------------------------------------
    // Polygon rebuilding — matches upstream EMFCube order: DOWN,UP,WEST,NORTH,EAST,SOUTH.
    // ---------------------------------------------------------------------

    private void rebuildStandardPolygons(float cubeX, float cubeY, float cubeZ,
                                         float sizeX, float sizeY, float sizeZ,
                                         float extraX, float extraY, float extraZ,
                                         float texU, float texV,
                                         float textureWidth, float textureHeight,
                                         boolean mirrorU, boolean mirrorV) {
        float cubeX2 = cubeX + sizeX;
        float cubeY2 = cubeY + sizeY;
        float cubeZ2 = cubeZ + sizeZ;
        cubeX -= extraX; cubeY -= extraY; cubeZ -= extraZ;
        cubeX2 += extraX; cubeY2 += extraY; cubeZ2 += extraZ;

        ModelPart.Vertex v0 = new ModelPart.Vertex(cubeX,  cubeY,  cubeZ,  0, 0);
        ModelPart.Vertex v1 = new ModelPart.Vertex(cubeX2, cubeY,  cubeZ,  0, 8);
        ModelPart.Vertex v2 = new ModelPart.Vertex(cubeX2, cubeY2, cubeZ,  8, 8);
        ModelPart.Vertex v3 = new ModelPart.Vertex(cubeX,  cubeY2, cubeZ,  8, 0);
        ModelPart.Vertex v4 = new ModelPart.Vertex(cubeX,  cubeY,  cubeZ2, 0, 0);
        ModelPart.Vertex v5 = new ModelPart.Vertex(cubeX2, cubeY,  cubeZ2, 0, 8);
        ModelPart.Vertex v6 = new ModelPart.Vertex(cubeX2, cubeY2, cubeZ2, 8, 8);
        ModelPart.Vertex v7 = new ModelPart.Vertex(cubeX,  cubeY2, cubeZ2, 8, 0);

        float j = texU;
        float k = texU + sizeZ;
        float l = texU + sizeZ + sizeX;
        float m = texU + sizeZ + sizeX + sizeX;
        float n = texU + sizeZ + sizeX + sizeZ;
        float o = texU + sizeZ + sizeX + sizeZ + sizeX;
        float p = texV;
        float q = texV + sizeZ;
        float r = texV + sizeZ + sizeY;

        // DOWN (index 0)
        this.polygons[0] = new ModelPart.Polygon(
                mirrorV ? new ModelPart.Vertex[]{v2, v3, v7, v6}
                        : new ModelPart.Vertex[]{v5, v4, v0, v1},
                mirrorU ? l : k, mirrorV ? q : p,
                mirrorU ? k : l, mirrorV ? p : q,
                textureWidth, textureHeight, false,
                mirrorV ? Direction.UP : Direction.DOWN);
        // UP (index 1)
        this.polygons[1] = new ModelPart.Polygon(
                mirrorV ? new ModelPart.Vertex[]{v5, v4, v0, v1}
                        : new ModelPart.Vertex[]{v2, v3, v7, v6},
                mirrorU ? m : l, mirrorV ? p : q,
                mirrorU ? l : m, mirrorV ? q : p,
                textureWidth, textureHeight, false,
                mirrorV ? Direction.DOWN : Direction.UP);
        // WEST (index 2)
        this.polygons[2] = new ModelPart.Polygon(
                mirrorU ? new ModelPart.Vertex[]{v5, v1, v2, v6}
                        : new ModelPart.Vertex[]{v0, v4, v7, v3},
                mirrorU ? k : j, mirrorV ? r : q,
                mirrorU ? j : k, mirrorV ? q : r,
                textureWidth, textureHeight, false,
                mirrorU ? Direction.EAST : Direction.WEST);
        // NORTH (index 3)
        this.polygons[3] = new ModelPart.Polygon(
                new ModelPart.Vertex[]{v1, v0, v3, v2},
                mirrorU ? l : k, mirrorV ? r : q,
                mirrorU ? k : l, mirrorV ? q : r,
                textureWidth, textureHeight, false,
                Direction.NORTH);
        // EAST (index 4)
        this.polygons[4] = new ModelPart.Polygon(
                mirrorU ? new ModelPart.Vertex[]{v0, v4, v7, v3}
                        : new ModelPart.Vertex[]{v5, v1, v2, v6},
                mirrorU ? n : l, mirrorV ? r : q,
                mirrorU ? l : n, mirrorV ? q : r,
                textureWidth, textureHeight, false,
                mirrorU ? Direction.WEST : Direction.EAST);
        // SOUTH (index 5)
        this.polygons[5] = new ModelPart.Polygon(
                new ModelPart.Vertex[]{v4, v5, v6, v7},
                mirrorU ? o : n, mirrorV ? r : q,
                mirrorU ? n : o, mirrorV ? q : r,
                textureWidth, textureHeight, false,
                Direction.SOUTH);
    }

    private void rebuildPerFacePolygons(float cubeX, float cubeY, float cubeZ,
                                        float sizeX, float sizeY, float sizeZ,
                                        float extraX, float extraY, float extraZ,
                                        float[] uvDown, float[] uvUp,
                                        float[] uvNorth, float[] uvSouth,
                                        float[] uvWest, float[] uvEast,
                                        float textureWidth, float textureHeight,
                                        boolean mirrorU, boolean mirrorV) {
        float cubeX2 = cubeX + sizeX;
        float cubeY2 = cubeY + sizeY;
        float cubeZ2 = cubeZ + sizeZ;
        cubeX -= extraX; cubeY -= extraY; cubeZ -= extraZ;
        cubeX2 += extraX; cubeY2 += extraY; cubeZ2 += extraZ;

        ModelPart.Vertex v0 = new ModelPart.Vertex(cubeX,  cubeY,  cubeZ,  0, 0);
        ModelPart.Vertex v1 = new ModelPart.Vertex(cubeX2, cubeY,  cubeZ,  0, 8);
        ModelPart.Vertex v2 = new ModelPart.Vertex(cubeX2, cubeY2, cubeZ,  8, 8);
        ModelPart.Vertex v3 = new ModelPart.Vertex(cubeX,  cubeY2, cubeZ,  8, 0);
        ModelPart.Vertex v4 = new ModelPart.Vertex(cubeX,  cubeY,  cubeZ2, 0, 0);
        ModelPart.Vertex v5 = new ModelPart.Vertex(cubeX2, cubeY,  cubeZ2, 0, 8);
        ModelPart.Vertex v6 = new ModelPart.Vertex(cubeX2, cubeY2, cubeZ2, 8, 8);
        ModelPart.Vertex v7 = new ModelPart.Vertex(cubeX,  cubeY2, cubeZ2, 8, 0);

        // Upstream note: in the per-face UV constructor the first two slots map to:
        //   slot[0] = uvUp arrays drawn on the DOWN face-vertices (quirk of OptiFine)
        //   slot[1] = uvDown arrays drawn on the UP face-vertices
        // We preserve the quirk 1:1 so FA renders identically.
        this.polygons[0] = makeFacePolygon(
                mirrorV ? new ModelPart.Vertex[]{v7, v6, v2, v3}
                        : new ModelPart.Vertex[]{v0, v1, v5, v4},
                uvUp, textureWidth, textureHeight, mirrorU, mirrorV,
                mirrorV ? Direction.UP : Direction.DOWN);
        this.polygons[1] = makeFacePolygon(
                mirrorV ? new ModelPart.Vertex[]{v0, v1, v5, v4}
                        : new ModelPart.Vertex[]{v7, v6, v2, v3},
                uvDown, textureWidth, textureHeight, mirrorU, mirrorV,
                mirrorV ? Direction.DOWN : Direction.UP);
        this.polygons[2] = makeFacePolygon(
                mirrorU ? new ModelPart.Vertex[]{v0, v4, v7, v3}
                        : new ModelPart.Vertex[]{v5, v1, v2, v6},
                uvWest, textureWidth, textureHeight, mirrorU, mirrorV,
                mirrorU ? Direction.WEST : Direction.EAST);
        this.polygons[3] = makeFacePolygon(
                new ModelPart.Vertex[]{v1, v0, v3, v2},
                uvNorth, textureWidth, textureHeight, mirrorU, mirrorV,
                Direction.NORTH);
        this.polygons[4] = makeFacePolygon(
                mirrorU ? new ModelPart.Vertex[]{v5, v1, v2, v6}
                        : new ModelPart.Vertex[]{v0, v4, v7, v3},
                uvEast, textureWidth, textureHeight, mirrorU, mirrorV,
                mirrorU ? Direction.EAST : Direction.WEST);
        this.polygons[5] = makeFacePolygon(
                new ModelPart.Vertex[]{v4, v5, v6, v7},
                uvSouth, textureWidth, textureHeight, mirrorU, mirrorV,
                Direction.SOUTH);
    }

    private static ModelPart.Polygon makeFacePolygon(ModelPart.Vertex[] verts, float[] uv,
                                                     float texW, float texH,
                                                     boolean mirrorU, boolean mirrorV,
                                                     Direction face) {
        if (uv == null || uv.length != 4) {
            // Fallback: draw with zero UV so the face exists but renders as a pinhole.
            return new ModelPart.Polygon(verts, 0, 0, 0, 0, texW, texH, false, face);
        }
        return new ModelPart.Polygon(
                verts,
                mirrorU ? uv[2] : uv[0], mirrorV ? uv[3] : uv[1],
                mirrorU ? uv[0] : uv[2], mirrorV ? uv[1] : uv[3],
                texW, texH, false, face);
    }
}
