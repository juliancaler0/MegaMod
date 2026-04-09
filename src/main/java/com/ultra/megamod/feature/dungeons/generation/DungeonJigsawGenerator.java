package com.ultra.megamod.feature.dungeons.generation;

import com.mojang.datafixers.util.Either;
import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.dungeons.DungeonTier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.pools.DimensionPadding;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasLookup;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure.MaxDistance;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;

import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Data-driven jigsaw dungeon generator that delegates to vanilla's
 * {@link JigsawPlacement#addPieces} engine. Template pools, processor lists,
 * and weights are all read from JSON datapacks — no hardcoded block swaps.
 * <p>
 * The vanilla engine handles: pool resolution, weighted element selection,
 * processor application (degradation, cobwebs, decor), rotation, overlap
 * checks, and fallback pool resolution.
 * <p>
 * We just build a GenerationContext, call addPieces, then postProcess each
 * piece into the pocket dimension ServerLevel.
 */
public class DungeonJigsawGenerator {

    // ========================= RESULT CLASS =========================

    public static final class DungeonLayout {
        private final List<BoundingBox> allPieces;
        private final BoundingBox bossRoom;
        private final BlockPos entrancePos;
        private final int totalPieces;

        public DungeonLayout(List<BoundingBox> allPieces, BoundingBox bossRoom,
                             BlockPos entrancePos, int totalPieces) {
            this.allPieces = allPieces;
            this.bossRoom = bossRoom;
            this.entrancePos = entrancePos;
            this.totalPieces = totalPieces;
        }

        public List<BoundingBox> getAllPieces() { return allPieces; }
        public BoundingBox getBossRoom() { return bossRoom; }
        public BlockPos getEntrancePos() { return entrancePos; }
        public int getTotalPieces() { return totalPieces; }
    }

    // ========================= PUBLIC API =========================

    /**
     * Generate a complete jigsaw dungeon using vanilla's jigsaw engine.
     * Reads template pools + processor lists from JSON datapacks.
     *
     * @param level  the dungeon pocket dimension (ServerLevel)
     * @param origin the pocket origin block position
     * @param tier   difficulty tier — controls jigsaw depth
     * @param random random source
     * @return a {@link DungeonLayout} with piece bounds, boss room, and entrance
     */
    public static DungeonLayout generate(ServerLevel level, BlockPos origin, DungeonTier tier,
                                          RandomSource random) {
        int maxDepth = tier.getJigsawDepth();
        int maxDist = tier.getMaxDistance();
        int maxPieces = tier.getMaxPieces();

        MegaMod.LOGGER.info("DungeonJigsawGenerator: Starting {} dungeon (depth={}, maxDist={}, maxPieces={})",
                tier.getDisplayName(), maxDepth, maxDist, maxPieces);

        // ===================================================================
        // Step 1: Resolve the start pool from the datapack registry
        // ===================================================================
        Registry<StructureTemplatePool> poolRegistry;
        Holder.Reference<StructureTemplatePool> startPool;
        try {
            poolRegistry = level.registryAccess().lookupOrThrow(Registries.TEMPLATE_POOL);
            startPool = poolRegistry.getOrThrow(
                    ResourceKey.create(Registries.TEMPLATE_POOL,
                            Identifier.fromNamespaceAndPath("dungeonnowloading", "labyrinth/starts")));
        } catch (Exception e) {
            MegaMod.LOGGER.error("DungeonJigsawGenerator: Failed to resolve start pool from registry", e);
            return fallbackLayout(level, origin);
        }

        // ===================================================================
        // Step 2: Build GenerationContext from the ServerLevel
        // ===================================================================
        ChunkGenerator chunkGen = level.getChunkSource().getGenerator();
        ChunkPos chunkPos = new ChunkPos(origin);

        // Force-load the chunk at origin so placement works
        level.getChunk(origin);

        Structure.GenerationContext ctx = new Structure.GenerationContext(
                level.registryAccess(),
                chunkGen,
                chunkGen.getBiomeSource(),
                level.getChunkSource().randomState(),
                level.getStructureManager(),
                level.getSeed(),
                chunkPos,
                level,
                (biome) -> true  // accept all biomes in pocket dimension
        );

        // ===================================================================
        // Step 3: Call vanilla's JigsawPlacement.addPieces()
        // This does ALL the heavy lifting: pool resolution, weighted element
        // selection, processor application, rotation, overlap checks, fallbacks
        // ===================================================================
        // Match DNL: use "dungeonnowloading:start" as the start jigsaw name
        // This anchors the structure at the correct jigsaw connector in boss_room.nbt
        Optional<Structure.GenerationStub> result = JigsawPlacement.addPieces(
                ctx,
                startPool,
                Optional.of(Identifier.fromNamespaceAndPath("dungeonnowloading", "start")),
                maxDepth,
                origin,
                false,                      // no expansion hack
                Optional.empty(),           // no heightmap projection
                new MaxDistance(maxDist),   // tier-scaled max distance from center
                PoolAliasLookup.EMPTY,      // no pool aliases
                DimensionPadding.ZERO,      // no dimension padding
                LiquidSettings.APPLY_WATERLOGGING
        );

        if (result.isEmpty()) {
            MegaMod.LOGGER.error("DungeonJigsawGenerator: JigsawPlacement.addPieces returned empty");
            return fallbackLayout(level, origin);
        }

        // ===================================================================
        // Step 4: Extract pieces from the GenerationStub
        // ===================================================================
        Either<Consumer<StructurePiecesBuilder>, StructurePiecesBuilder> generator =
                result.get().generator();

        StructurePiecesBuilder builder = new StructurePiecesBuilder();
        generator.ifLeft(consumer -> consumer.accept(builder));
        // ifRight means pieces are already in the builder — but we created a fresh one,
        // so we only handle the left (consumer) case

        var builtPieces = builder.build();
        List<StructurePiece> pieces = new ArrayList<>(builtPieces.pieces());

        if (pieces.isEmpty()) {
            MegaMod.LOGGER.warn("DungeonJigsawGenerator: No pieces generated");
            return fallbackLayout(level, origin);
        }

        MegaMod.LOGGER.info("DungeonJigsawGenerator: Vanilla jigsaw produced {} pieces (tier={}, depth={}, maxDist={})",
                pieces.size(), tier.getDisplayName(), maxDepth, maxDist);
        // No piece capping — jigsaw pieces include rooms + all their child decor/mob/spawner
        // elements. Removing connected pieces breaks room rendering. Dungeon size is controlled
        // via jigsaw depth and max distance instead.

        // ===================================================================
        // Step 5: Force-load all chunks the dungeon spans, then place pieces
        // postProcess only writes blocks in loaded chunks, so we must ensure
        // every chunk touched by every piece is loaded first.
        // ===================================================================
        for (StructurePiece piece : pieces) {
            BoundingBox bb = piece.getBoundingBox();
            int minCX = bb.minX() >> 4;
            int maxCX = bb.maxX() >> 4;
            int minCZ = bb.minZ() >> 4;
            int maxCZ = bb.maxZ() >> 4;
            for (int cx = minCX; cx <= maxCX; cx++) {
                for (int cz = minCZ; cz <= maxCZ; cz++) {
                    level.getChunk(cx, cz);
                }
            }
        }

        StructureManager structureManager = level.structureManager();

        // postProcess places blocks only in the chunk identified by the chunkPos parameter.
        // For multi-chunk dungeons, we must call postProcess once per chunk that each piece spans.
        for (StructurePiece piece : pieces) {
            BoundingBox bb = piece.getBoundingBox();
            int minCX = bb.minX() >> 4;
            int maxCX = bb.maxX() >> 4;
            int minCZ = bb.minZ() >> 4;
            int maxCZ = bb.maxZ() >> 4;
            for (int cx = minCX; cx <= maxCX; cx++) {
                for (int cz = minCZ; cz <= maxCZ; cz++) {
                    ChunkPos pieceChunkPos = new ChunkPos(cx, cz);
                    // boundingBox param clips which blocks get placed within this chunk
                    BoundingBox chunkBounds = new BoundingBox(
                            cx << 4, bb.minY(), cz << 4,
                            (cx << 4) + 15, bb.maxY(), (cz << 4) + 15
                    );
                    piece.postProcess(
                            level,
                            structureManager,
                            chunkGen,
                            random,
                            chunkBounds,
                            pieceChunkPos,
                            origin
                    );
                }
            }
        }

        // ===================================================================
        // Step 6: Build DungeonLayout — boss room is the START piece (grand_hall,
        // matching DNL's layout where the dungeon starts in the boss room).
        // The player spawns in the boss room and explores outward.
        // ===================================================================
        List<BoundingBox> allBounds = new ArrayList<>();
        BoundingBox bossRoomBounds = null;
        BlockPos entrancePos = origin.above();

        for (int i = 0; i < pieces.size(); i++) {
            BoundingBox bb = pieces.get(i).getBoundingBox();
            allBounds.add(bb);
            if (i == 0) {
                // First piece = grand_hall = boss room (matches DNL starts.json)
                bossRoomBounds = bb;
            }
        }

        if (bossRoomBounds != null) {
            MegaMod.LOGGER.info("DungeonJigsawGenerator: Start piece (grand_hall) is boss room: {}",
                    bossRoomBounds);
        } else {
            MegaMod.LOGGER.warn("DungeonJigsawGenerator: No pieces generated for boss room!");
        }

        // Entrance position: pick a random non-boss room so players must explore to find the boss.
        // If only 1 piece exists, fall back to the boss room.
        if (!pieces.isEmpty()) {
            BoundingBox spawnRoom;
            if (pieces.size() > 1) {
                // Pick a random non-boss piece (skip index 0 = boss room)
                int spawnIdx = 1 + random.nextInt(pieces.size() - 1);
                spawnRoom = pieces.get(spawnIdx).getBoundingBox();
                MegaMod.LOGGER.info("DungeonJigsawGenerator: Randomized spawn to piece {} of {}", spawnIdx, pieces.size());
            } else {
                spawnRoom = pieces.get(0).getBoundingBox();
            }

            int centerX = spawnRoom.minX() + (spawnRoom.maxX() - spawnRoom.minX()) / 2;
            int centerZ = spawnRoom.minZ() + (spawnRoom.maxZ() - spawnRoom.minZ()) / 2;
            // Scan upward through the bounding box to find air above solid floor
            BlockPos found = findSpawnablePos(level, centerX, centerZ, spawnRoom);
            if (found == null) {
                // Try random offsets within the room
                for (int attempt = 0; attempt < 8 && found == null; attempt++) {
                    int offX = centerX + random.nextIntBetweenInclusive(-3, 3);
                    int offZ = centerZ + random.nextIntBetweenInclusive(-3, 3);
                    found = findSpawnablePos(level, offX, offZ, spawnRoom);
                }
            }
            if (found == null) {
                // Full scan: iterate every X/Z in the room interior (skip outer edge = walls)
                for (int sx = spawnRoom.minX() + 2; sx <= spawnRoom.maxX() - 2 && found == null; sx++) {
                    for (int sz = spawnRoom.minZ() + 2; sz <= spawnRoom.maxZ() - 2 && found == null; sz++) {
                        found = findSpawnablePos(level, sx, sz, spawnRoom);
                    }
                }
            }
            if (found == null) {
                // Last resort: center of room at minY + 3 — clear a small pocket so player isn't stuck
                found = new BlockPos(centerX, spawnRoom.minY() + 3, centerZ);
                MegaMod.LOGGER.warn("DungeonJigsawGenerator: No valid spawn found, clearing pocket at {}", found);
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        for (int dy = 0; dy <= 2; dy++) {
                            level.setBlockAndUpdate(found.offset(dx, dy, dz),
                                    net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
                        }
                    }
                }
                // Place a solid floor beneath
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        BlockPos below = found.offset(dx, -1, dz);
                        if (level.getBlockState(below).isAir()) {
                            level.setBlockAndUpdate(below,
                                    net.minecraft.world.level.block.Blocks.STONE_BRICKS.defaultBlockState());
                        }
                    }
                }
            }
            entrancePos = found;
            MegaMod.LOGGER.info("DungeonJigsawGenerator: Entrance validated at {}", entrancePos);
        }

        int totalPieces = allBounds.size();
        MegaMod.LOGGER.info("DungeonJigsawGenerator: Complete. {} pieces placed, bossRoom={}",
                totalPieces, bossRoomBounds != null);

        return new DungeonLayout(allBounds, bossRoomBounds, entrancePos, totalPieces);
    }

    // ========================= HELPERS =========================

    /**
     * Scans upward at (x, z) within the bounding box to find solid floor with 2 air blocks above
     * and enough horizontal clearance so the player doesn't spawn inside a wall.
     * Returns the air block above the floor, or null if no valid spot found.
     */
    private static BlockPos findSpawnablePos(ServerLevel level, int x, int z, BoundingBox bb) {
        for (int y = bb.minY(); y <= bb.maxY() - 2; y++) {
            BlockPos candidate = new BlockPos(x, y, z);
            if (!level.getBlockState(candidate).isAir()
                    && !(level.getBlockState(candidate).getBlock() instanceof com.ultra.megamod.feature.dungeons.block.SpikeBlock)
                    && level.getBlockState(candidate.above()).isAir()
                    && level.getBlockState(candidate.above(2)).isAir()
                    && hasHorizontalClearance(level, candidate.above())) {
                return candidate.above();
            }
        }
        return null;
    }

    /**
     * Checks that a position has enough horizontal clearance for a player (no adjacent walls).
     * The player hitbox is ~0.6 blocks wide, so being at a block center next to a solid block
     * can clip into the wall. Checks all 4 cardinal neighbors at feet and head height.
     */
    private static boolean hasHorizontalClearance(ServerLevel level, BlockPos feetPos) {
        for (BlockPos neighbor : new BlockPos[]{feetPos.north(), feetPos.south(), feetPos.east(), feetPos.west()}) {
            if (!level.getBlockState(neighbor).isAir() || !level.getBlockState(neighbor.above()).isAir()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Error layout when jigsaw generation fails — places a solid floor so the
     * player doesn't fall into the void, with walls and a return portal area.
     */
    private static DungeonLayout fallbackLayout(ServerLevel level, BlockPos origin) {
        BlockState stoneBricks = net.minecraft.world.level.block.Blocks.STONE_BRICKS.defaultBlockState();
        BlockState glowstone = net.minecraft.world.level.block.Blocks.GLOWSTONE.defaultBlockState();
        BlockState air = net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
        int half = 8;
        int height = 6;

        for (int dx = -half; dx <= half; dx++) {
            for (int dz = -half; dz <= half; dz++) {
                boolean isWall = (dx == -half || dx == half || dz == -half || dz == half);
                // Floor
                level.setBlock(origin.offset(dx, 0, dz), stoneBricks, 2);
                // Ceiling
                boolean isLight = (dx % 4 == 0) && (dz % 4 == 0) && !isWall;
                level.setBlock(origin.offset(dx, height, dz), isLight ? glowstone : stoneBricks, 2);
                // Walls / air
                for (int y = 1; y < height; y++) {
                    level.setBlock(origin.offset(dx, y, dz), isWall ? stoneBricks : air, 2);
                }
            }
        }

        BoundingBox room = new BoundingBox(
                origin.getX() - half, origin.getY(), origin.getZ() - half,
                origin.getX() + half, origin.getY() + height, origin.getZ() + half
        );
        MegaMod.LOGGER.error("DungeonJigsawGenerator: Using fallback room — jigsaw generation failed!");
        return new DungeonLayout(List.of(room), room, origin.above(), 1);
    }

    private static int distSq(BoundingBox bb, BlockPos origin) {
        int cx = (bb.minX() + bb.maxX()) / 2 - origin.getX();
        int cz = (bb.minZ() + bb.maxZ()) / 2 - origin.getZ();
        return cx * cx + cz * cz;
    }
}
