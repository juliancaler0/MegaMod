package com.ultra.megamod.feature.worldedit;

import com.ultra.megamod.feature.worldedit.history.BlockChange;
import com.ultra.megamod.feature.worldedit.history.ChangeSet;
import com.ultra.megamod.feature.worldedit.mask.Mask;
import com.ultra.megamod.feature.worldedit.pattern.Pattern;
import com.ultra.megamod.feature.worldedit.region.Region;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The core WorldEdit mutation engine. An EditSession buffers all block
 * mutations and records them into a ChangeSet. Operations respect an
 * optional mask.
 */
public class EditSession {

    /** Default hard cap on blocks modified per operation. */
    public static final int DEFAULT_MAX_CHANGES = 500_000;

    private final ServerLevel level;
    private final ChangeSet changes;
    private final int maxChanges;
    private Mask mask;
    private int blocksChanged = 0;
    private boolean aborted = false;

    public EditSession(ServerLevel level) {
        this(level, DEFAULT_MAX_CHANGES);
    }

    public EditSession(ServerLevel level, int maxChanges) {
        this.level = level;
        this.maxChanges = maxChanges;
        this.changes = new ChangeSet(level.dimension());
    }

    public ServerLevel getLevel() { return level; }
    public ChangeSet getChangeSet() { return changes; }
    public int getBlocksChanged() { return blocksChanged; }
    public int getMaxChanges() { return maxChanges; }

    public Mask getMask() { return mask; }
    public void setMask(Mask m) { this.mask = m; }

    /** Attempts to set a block. Returns true if the block was changed. */
    public boolean setBlock(BlockPos pos, BlockState state) {
        if (aborted) return false;
        if (blocksChanged >= maxChanges) {
            aborted = true;
            return false;
        }
        if (mask != null && !mask.test(level, pos)) return false;

        BlockState old = level.getBlockState(pos);
        if (old == state) return false;

        level.setBlock(pos, state, 2);
        changes.add(new BlockChange(pos, old, state));
        blocksChanged++;
        return true;
    }

    public boolean setBlock(BlockPos pos, Pattern pattern) {
        BlockState st = pattern.apply(pos);
        if (st == null) return false;
        return setBlock(pos, st);
    }

    /** Sets every block in the region using the pattern. */
    public int set(Region region, Pattern pattern) {
        int count = 0;
        for (BlockPos p : region) {
            if (setBlock(p, pattern)) count++;
            if (aborted) break;
        }
        return count;
    }

    /** Replaces any block matching the filter mask with the pattern. */
    public int replace(Region region, Mask filter, Pattern pattern) {
        int count = 0;
        for (BlockPos p : region) {
            if (filter != null && !filter.test(level, p)) continue;
            if (setBlock(p, pattern)) count++;
            if (aborted) break;
        }
        return count;
    }

    /** Fills the outline (hollow shell) of the region. */
    public int makeWalls(Region region, Pattern pattern) {
        int count = 0;
        BlockPos min = region.getMinimumPoint();
        BlockPos max = region.getMaximumPoint();
        for (BlockPos p : region) {
            if (p.getX() == min.getX() || p.getX() == max.getX()
             || p.getZ() == min.getZ() || p.getZ() == max.getZ()) {
                if (setBlock(p, pattern)) count++;
            }
            if (aborted) break;
        }
        return count;
    }

    /** Fills faces on all six sides of the region. */
    public int makeFaces(Region region, Pattern pattern) {
        int count = 0;
        BlockPos min = region.getMinimumPoint();
        BlockPos max = region.getMaximumPoint();
        for (BlockPos p : region) {
            if (p.getX() == min.getX() || p.getX() == max.getX()
             || p.getY() == min.getY() || p.getY() == max.getY()
             || p.getZ() == min.getZ() || p.getZ() == max.getZ()) {
                if (setBlock(p, pattern)) count++;
            }
            if (aborted) break;
        }
        return count;
    }

    /** Overlays the top solid surface of the region with the pattern. */
    public int overlay(Region region, Pattern pattern) {
        int count = 0;
        BlockPos min = region.getMinimumPoint();
        BlockPos max = region.getMaximumPoint();
        for (int x = min.getX(); x <= max.getX(); x++) {
            for (int z = min.getZ(); z <= max.getZ(); z++) {
                for (int y = max.getY(); y >= min.getY(); y--) {
                    BlockPos p = new BlockPos(x, y, z);
                    if (!region.contains(p)) continue;
                    BlockState here = level.getBlockState(p);
                    BlockPos above = p.above();
                    if (!here.isAir() && level.getBlockState(above).isAir()) {
                        if (setBlock(above, pattern)) count++;
                        break;
                    }
                }
            }
            if (aborted) break;
        }
        return count;
    }

    /** Replaces all non-air blocks in the region with air. */
    public int remove(Region region) {
        return replace(region, new com.ultra.megamod.feature.worldedit.mask.ExistingBlockMask(),
                new com.ultra.megamod.feature.worldedit.pattern.BlockPattern(Blocks.AIR.defaultBlockState()));
    }

    /** Deletes every block above the region in the given height. */
    public int removeAbove(BlockPos pos, int size, int height) {
        int count = 0;
        int half = size / 2;
        for (int x = pos.getX() - half; x <= pos.getX() + half; x++) {
            for (int z = pos.getZ() - half; z <= pos.getZ() + half; z++) {
                for (int y = pos.getY() + 1; y <= pos.getY() + height; y++) {
                    BlockPos p = new BlockPos(x, y, z);
                    if (setBlock(p, Blocks.AIR.defaultBlockState())) count++;
                }
            }
        }
        return count;
    }

    public int removeBelow(BlockPos pos, int size, int height) {
        int count = 0;
        int half = size / 2;
        for (int x = pos.getX() - half; x <= pos.getX() + half; x++) {
            for (int z = pos.getZ() - half; z <= pos.getZ() + half; z++) {
                for (int y = pos.getY() - 1; y >= pos.getY() - height; y--) {
                    BlockPos p = new BlockPos(x, y, z);
                    if (setBlock(p, Blocks.AIR.defaultBlockState())) count++;
                }
            }
        }
        return count;
    }

    public int removeNear(BlockPos pos, int size, int height) {
        int count = 0;
        int half = size / 2;
        for (int x = pos.getX() - half; x <= pos.getX() + half; x++) {
            for (int y = pos.getY() - height; y <= pos.getY() + height; y++) {
                for (int z = pos.getZ() - half; z <= pos.getZ() + half; z++) {
                    BlockPos p = new BlockPos(x, y, z);
                    if (setBlock(p, Blocks.AIR.defaultBlockState())) count++;
                }
            }
        }
        return count;
    }

    /** Creates a hollow sphere centered on origin with the given radii. */
    public int makeSphere(BlockPos origin, int radiusX, int radiusY, int radiusZ, Pattern pattern, boolean filled) {
        int count = 0;
        double rx2 = radiusX * radiusX;
        double ry2 = radiusY * radiusY;
        double rz2 = radiusZ * radiusZ;
        for (int x = -radiusX; x <= radiusX; x++) {
            for (int y = -radiusY; y <= radiusY; y++) {
                for (int z = -radiusZ; z <= radiusZ; z++) {
                    double dx = x / (double) Math.max(1, radiusX);
                    double dy = y / (double) Math.max(1, radiusY);
                    double dz = z / (double) Math.max(1, radiusZ);
                    double d = dx * dx + dy * dy + dz * dz;
                    if (d > 1.0) continue;
                    if (!filled) {
                        // only draw if neighbor outside the shell
                        double dxo = (Math.abs(x) + 1) / (double) Math.max(1, radiusX);
                        double dyo = (Math.abs(y) + 1) / (double) Math.max(1, radiusY);
                        double dzo = (Math.abs(z) + 1) / (double) Math.max(1, radiusZ);
                        if (dxo * dxo + dy * dy + dz * dz <= 1.0
                         && dx * dx + dyo * dyo + dz * dz <= 1.0
                         && dx * dx + dy * dy + dzo * dzo <= 1.0) continue;
                    }
                    if (setBlock(origin.offset(x, y, z), pattern)) count++;
                }
            }
            if (aborted) break;
        }
        return count;
    }

    public int makeCylinder(BlockPos origin, Pattern pattern, int radiusX, int radiusZ, int height, boolean filled) {
        int count = 0;
        int signH = height < 0 ? -1 : 1;
        height = Math.abs(height);
        for (int y = 0; y < height; y++) {
            for (int x = -radiusX; x <= radiusX; x++) {
                for (int z = -radiusZ; z <= radiusZ; z++) {
                    double dx = x / (double) Math.max(1, radiusX);
                    double dz = z / (double) Math.max(1, radiusZ);
                    double d = dx * dx + dz * dz;
                    if (d > 1.0) continue;
                    if (!filled) {
                        double dxo = (Math.abs(x) + 1) / (double) Math.max(1, radiusX);
                        double dzo = (Math.abs(z) + 1) / (double) Math.max(1, radiusZ);
                        if (dxo * dxo + dz * dz <= 1.0 && dx * dx + dzo * dzo <= 1.0) continue;
                    }
                    if (setBlock(origin.offset(x, y * signH, z), pattern)) count++;
                }
            }
            if (aborted) break;
        }
        return count;
    }

    public int makePyramid(BlockPos origin, Pattern pattern, int size, boolean filled) {
        int count = 0;
        for (int y = 0; y <= size; y++) {
            int h = size - y;
            for (int x = -h; x <= h; x++) {
                for (int z = -h; z <= h; z++) {
                    if (filled || (Math.abs(x) == h || Math.abs(z) == h || y == size)) {
                        if (setBlock(origin.offset(x, y, z), pattern)) count++;
                    }
                }
            }
        }
        return count;
    }

    /** Places blocks on every solid block in the region (drape). */
    public int drainPool(BlockPos origin, int radius) {
        int count = 0;
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    if (x * x + y * y + z * z > radius * radius) continue;
                    BlockPos p = origin.offset(x, y, z);
                    BlockState s = level.getBlockState(p);
                    if (s.getFluidState().isEmpty()) continue;
                    if (setBlock(p, Blocks.AIR.defaultBlockState())) count++;
                }
            }
        }
        return count;
    }

    public int fixWater(BlockPos origin, int radius) {
        return fixFluid(origin, radius, Blocks.WATER.defaultBlockState());
    }

    public int fixLava(BlockPos origin, int radius) {
        return fixFluid(origin, radius, Blocks.LAVA.defaultBlockState());
    }

    private int fixFluid(BlockPos origin, int radius, BlockState fill) {
        int count = 0;
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    if (x * x + y * y + z * z > radius * radius) continue;
                    BlockPos p = origin.offset(x, y, z);
                    BlockState s = level.getBlockState(p);
                    if (s.getBlock() == fill.getBlock()) {
                        if (setBlock(p, fill)) count++;
                    }
                }
            }
        }
        return count;
    }

    /** Capture block-entity NBT into a map (used for copy ops). */
    public Map<BlockPos, CompoundTag> captureBlockEntities(Region region) {
        Map<BlockPos, CompoundTag> map = new HashMap<>();
        for (BlockPos p : region) {
            BlockEntity be = level.getBlockEntity(p);
            if (be != null) {
                CompoundTag tag = be.saveWithFullMetadata(level.registryAccess());
                map.put(p.immutable(), tag);
            }
        }
        return map;
    }

    public boolean isAborted() { return aborted; }

    /** Returns a snapshot list of all changes recorded so far. */
    public List<BlockChange> snapshotChanges() { return new ArrayList<>(changes.changes()); }
}
