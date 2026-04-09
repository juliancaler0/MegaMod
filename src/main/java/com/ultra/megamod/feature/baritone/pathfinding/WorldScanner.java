package com.ultra.megamod.feature.baritone.pathfinding;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;

import java.util.*;

/**
 * Efficient block scanning — section-by-section, Y-biased, radius-limited, chunk-aware.
 * Scans for specific block types in loaded chunks around a position.
 */
public class WorldScanner {
    private final ServerLevel level;

    public WorldScanner(ServerLevel level) {
        this.level = level;
    }

    /**
     * Scan for blocks of the given types within radius, returning positions sorted by distance.
     *
     * @param center    Center of scan
     * @param targets   Block types to find
     * @param radius    Horizontal scan radius in blocks
     * @param maxResults Maximum number of results
     * @return Sorted list of block positions (nearest first)
     */
    public List<BlockPos> scan(BlockPos center, Set<Block> targets, int radius, int maxResults) {
        List<BlockPos> results = new ArrayList<>();
        int chunkRadius = (radius >> 4) + 1;
        int centerCX = center.getX() >> 4;
        int centerCZ = center.getZ() >> 4;

        // Scan chunks in a spiral from center outward for better results ordering
        for (int ring = 0; ring <= chunkRadius; ring++) {
            List<int[]> chunks = getChunksInRing(centerCX, centerCZ, ring);
            for (int[] chunk : chunks) {
                if (!level.hasChunk(chunk[0], chunk[1])) continue;
                LevelChunk lc = level.getChunk(chunk[0], chunk[1]);
                scanChunk(lc, center, targets, radius, results);
                if (results.size() >= maxResults * 4) break; // Overscan then trim
            }
            if (results.size() >= maxResults * 4) break;
        }

        // Sort by distance and trim
        results.sort(Comparator.comparingDouble(p -> p.distSqr(center)));
        if (results.size() > maxResults) {
            results = new ArrayList<>(results.subList(0, maxResults));
        }
        return results;
    }

    /**
     * Scan for a single block type (convenience overload).
     */
    public List<BlockPos> scan(BlockPos center, Block target, int radius, int maxResults) {
        return scan(center, Set.of(target), radius, maxResults);
    }

    private void scanChunk(LevelChunk chunk, BlockPos center, Set<Block> targets, int radius, List<BlockPos> results) {
        int baseX = chunk.getPos().getMinBlockX();
        int baseZ = chunk.getPos().getMinBlockZ();
        LevelChunkSection[] sections = chunk.getSections();

        // Scan from sections near player Y first (Y-biased)
        int centerSectionIdx = Math.max(0, Math.min(sections.length - 1,
            (center.getY() - level.getMinY()) >> 4));

        // Alternate: center, center-1, center+1, center-2, center+2, ...
        for (int offset = 0; offset < sections.length; offset++) {
            int[] indices = offset == 0
                ? new int[]{centerSectionIdx}
                : new int[]{centerSectionIdx - offset, centerSectionIdx + offset};

            for (int sectionIdx : indices) {
                if (sectionIdx < 0 || sectionIdx >= sections.length) continue;
                LevelChunkSection section = sections[sectionIdx];
                if (section == null || section.hasOnlyAir()) continue;

                int sectionY = level.getMinY() + (sectionIdx << 4);
                scanSection(section, baseX, sectionY, baseZ, center, targets, radius, results);
            }
        }
    }

    private void scanSection(LevelChunkSection section, int baseX, int baseY, int baseZ,
                             BlockPos center, Set<Block> targets, int radius, List<BlockPos> results) {
        int radiusSq = radius * radius;
        for (int lx = 0; lx < 16; lx++) {
            for (int lz = 0; lz < 16; lz++) {
                int wx = baseX + lx;
                int wz = baseZ + lz;
                // Quick XZ distance check
                int dxOuter = wx - center.getX();
                int dzOuter = wz - center.getZ();
                if (dxOuter * dxOuter + dzOuter * dzOuter > radiusSq) continue;

                for (int ly = 0; ly < 16; ly++) {
                    BlockState state = section.getBlockState(lx, ly, lz);
                    if (targets.contains(state.getBlock())) {
                        int wy = baseY + ly;
                        results.add(new BlockPos(wx, wy, wz));
                    }
                }
            }
        }
    }

    private List<int[]> getChunksInRing(int cx, int cz, int ring) {
        List<int[]> chunks = new ArrayList<>();
        if (ring == 0) {
            chunks.add(new int[]{cx, cz});
            return chunks;
        }
        // Walk around the ring perimeter
        for (int i = -ring; i <= ring; i++) {
            chunks.add(new int[]{cx + i, cz - ring});
            chunks.add(new int[]{cx + i, cz + ring});
        }
        for (int i = -ring + 1; i < ring; i++) {
            chunks.add(new int[]{cx - ring, cz + i});
            chunks.add(new int[]{cx + ring, cz + i});
        }
        return chunks;
    }
}
