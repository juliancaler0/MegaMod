package com.ultra.megamod.feature.baritone.pathfinding;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

/**
 * Enhanced BlockPos with precomputed hash and neighbor helpers.
 * Uses delegation instead of field shadowing for safety.
 */
public final class BetterBlockPos extends BlockPos {
    private static final int HASH_X_MULT = 0x45D9F3B;
    private static final int HASH_Z_MULT = 0x27D4EB2D;

    public final int x;
    public final int y;
    public final int z;
    private final int hash;

    public BetterBlockPos(int x, int y, int z) {
        super(x, y, z);
        this.x = x;
        this.y = y;
        this.z = z;
        this.hash = computeHash(x, y, z);
    }

    public BetterBlockPos(BlockPos pos) {
        this(pos.getX(), pos.getY(), pos.getZ());
    }

    private static int computeHash(int x, int y, int z) {
        int h = x * HASH_X_MULT;
        h ^= y * 0x3C6EF35F;
        h ^= z * HASH_Z_MULT;
        return h;
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public int getX() { return x; }

    @Override
    public int getY() { return y; }

    @Override
    public int getZ() { return z; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof BetterBlockPos other) {
            return this.x == other.x && this.y == other.y && this.z == other.z;
        }
        if (o instanceof BlockPos other) {
            return this.x == other.getX() && this.y == other.getY() && this.z == other.getZ();
        }
        return false;
    }

    public BetterBlockPos north() { return new BetterBlockPos(x, y, z - 1); }
    public BetterBlockPos south() { return new BetterBlockPos(x, y, z + 1); }
    public BetterBlockPos east()  { return new BetterBlockPos(x + 1, y, z); }
    public BetterBlockPos west()  { return new BetterBlockPos(x - 1, y, z); }
    public BetterBlockPos up()    { return new BetterBlockPos(x, y + 1, z); }
    public BetterBlockPos down()  { return new BetterBlockPos(x, y - 1, z); }

    public BetterBlockPos relative(Direction dir) {
        return new BetterBlockPos(x + dir.getStepX(), y + dir.getStepY(), z + dir.getStepZ());
    }

    public BetterBlockPos offset(int dx, int dy, int dz) {
        return new BetterBlockPos(x + dx, y + dy, z + dz);
    }

    public double distSq(BetterBlockPos other) {
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        double dz = this.z - other.z;
        return dx * dx + dy * dy + dz * dz;
    }

    @Override
    public String toString() {
        return "BetterBlockPos{" + x + ", " + y + ", " + z + "}";
    }
}
