package com.ultra.megamod.feature.citizen.blueprint.placement;

import net.minecraft.core.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default layer-by-layer blueprint iterator.
 * Iterates in Y -> Z -> X order (bottom to top, row by row, column by column).
 * Uses a serpentine pattern on X for each Z row to reduce travel distance.
 */
public class BlueprintIteratorDefault implements IBlueprintIterator {

    private static final Logger LOGGER = LoggerFactory.getLogger(BlueprintIteratorDefault.class);
    private static final BlockPos NULL_POS = new BlockPos(-1, -1, -1);

    private final int sizeX;
    private final int sizeY;
    private final int sizeZ;

    private final BlockPos.MutableBlockPos progressPos = new BlockPos.MutableBlockPos(-1, -1, -1);
    private boolean removing;
    private boolean includeEntities;

    /**
     * Create a default iterator for the given blueprint dimensions.
     *
     * @param sizeX blueprint width (X axis).
     * @param sizeY blueprint height (Y axis).
     * @param sizeZ blueprint depth (Z axis).
     */
    public BlueprintIteratorDefault(int sizeX, int sizeY, int sizeZ) {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;

        if (sizeX <= 0 || sizeY <= 0 || sizeZ <= 0) {
            LOGGER.warn("BlueprintIteratorDefault created with non-positive size: {}x{}x{}", sizeX, sizeY, sizeZ);
        }
    }

    @Override
    public Result increment() {
        return iterate(true);
    }

    @Override
    public Result decrement() {
        return iterate(false);
    }

    /**
     * Core iteration logic. Moves through X, then Z (serpentine), then Y.
     *
     * @param forward true for increment (Y upward), false for decrement (Y downward).
     * @return the iteration result.
     */
    private Result iterate(boolean forward) {
        // Initialize if at null position
        if (progressPos.equals(NULL_POS)) {
            if (forward) {
                progressPos.set(-1, 0, 0);
            } else {
                progressPos.set(sizeX, sizeY - 1, sizeZ - 1);
            }
        }

        // Serpentine pattern: even Z rows go left-to-right, odd Z rows go right-to-left
        if (progressPos.getZ() % 2 == 0) {
            // Moving X forward
            progressPos.set(progressPos.getX() + 1, progressPos.getY(), progressPos.getZ());
            if (progressPos.getX() >= sizeX) {
                // Wrap to next Z row, keep X at the end for serpentine
                progressPos.set(sizeX - 1, progressPos.getY(), progressPos.getZ() + 1);
                if (progressPos.getZ() >= sizeZ) {
                    // Wrap to next Y layer
                    int nextY = forward ? progressPos.getY() + 1 : progressPos.getY() - 1;
                    progressPos.set(0, nextY, 0);
                    if ((forward && nextY >= sizeY) || (!forward && nextY < 0)) {
                        reset();
                        return Result.AT_END;
                    }
                }
            }
        } else {
            // Moving X backward (serpentine)
            progressPos.set(progressPos.getX() - 1, progressPos.getY(), progressPos.getZ());
            if (progressPos.getX() < 0) {
                // Wrap to next Z row, start X at 0 for serpentine
                progressPos.set(0, progressPos.getY(), progressPos.getZ() + 1);
                if (progressPos.getZ() >= sizeZ) {
                    // Wrap to next Y layer
                    int nextY = forward ? progressPos.getY() + 1 : progressPos.getY() - 1;
                    progressPos.set(0, nextY, 0);
                    if ((forward && nextY >= sizeY) || (!forward && nextY < 0)) {
                        reset();
                        return Result.AT_END;
                    }
                }
            }
        }

        return Result.NEW_BLOCK;
    }

    @Override
    public BlockPos getProgressPos() {
        return progressPos.immutable();
    }

    @Override
    public void setProgressPos(BlockPos pos) {
        if (pos == null || pos.equals(NULL_POS)) {
            progressPos.set(NULL_POS);
        } else {
            // Clamp to valid range
            progressPos.set(
                Math.max(-1, Math.min(pos.getX(), sizeX)),
                Math.max(-1, Math.min(pos.getY(), sizeY)),
                Math.max(-1, Math.min(pos.getZ(), sizeZ))
            );
        }
    }

    @Override
    public boolean isRemoving() {
        return removing;
    }

    @Override
    public void setRemoving(boolean removing) {
        this.removing = removing;
    }

    @Override
    public boolean includeEntities() {
        return includeEntities;
    }

    @Override
    public void setIncludeEntities(boolean include) {
        this.includeEntities = include;
    }

    @Override
    public void reset() {
        progressPos.set(NULL_POS);
        removing = false;
        includeEntities = false;
    }

    @Override
    public BlockPos getSize() {
        return new BlockPos(sizeX, sizeY, sizeZ);
    }
}
