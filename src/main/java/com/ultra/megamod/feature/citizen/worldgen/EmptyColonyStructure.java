package com.ultra.megamod.feature.citizen.worldgen;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import org.slf4j.Logger;

import java.util.Optional;

/**
 * Custom jigsaw-based structure for spawning empty colonies in the world.
 * Based on MineColonies EmptyColonyStructure.
 */
public class EmptyColonyStructure extends Structure {

    private static final Logger LOGGER = LogUtils.getLogger();

    public static final MapCodec<EmptyColonyStructure> COLONY_CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    EmptyColonyStructure.settingsCodec(instance),
                    StructureTemplatePool.CODEC.fieldOf("start_pool").forGetter(s -> s.startPool),
                    Identifier.CODEC.optionalFieldOf("start_jigsaw_name").forGetter(s -> s.startJigsawName),
                    Codec.intRange(0, 10).fieldOf("size").forGetter(s -> s.size),
                    HeightProvider.CODEC.fieldOf("start_height").forGetter(s -> s.startHeight),
                    Heightmap.Types.CODEC.optionalFieldOf("project_start_to_heightmap").forGetter(s -> s.projectStartToHeightmap),
                    Codec.intRange(1, 128).fieldOf("max_distance_from_center").forGetter(s -> s.maxDistanceFromCenter),
                    Codec.BOOL.optionalFieldOf("allow_cave", false).forGetter(s -> s.allowCave)
            ).apply(instance, EmptyColonyStructure::new));

    private final Holder<StructureTemplatePool> startPool;
    private final Optional<Identifier> startJigsawName;
    private final int size;
    private final HeightProvider startHeight;
    private final Optional<Heightmap.Types> projectStartToHeightmap;
    private final int maxDistanceFromCenter;
    private final boolean allowCave;

    public EmptyColonyStructure(Structure.StructureSettings config,
                                Holder<StructureTemplatePool> startPool,
                                Optional<Identifier> startJigsawName,
                                int size,
                                HeightProvider startHeight,
                                Optional<Heightmap.Types> projectStartToHeightmap,
                                int maxDistanceFromCenter,
                                boolean allowCave) {
        super(config);
        this.startPool = startPool;
        this.startJigsawName = startJigsawName;
        this.size = size;
        this.startHeight = startHeight;
        this.projectStartToHeightmap = projectStartToHeightmap;
        this.maxDistanceFromCenter = maxDistanceFromCenter;
        this.allowCave = allowCave;
    }

    @Override
    public StructureType<?> type() {
        return ColonyWorldGenRegistry.EMPTY_COLONY.get();
    }

    @Override
    public GenerationStep.Decoration step() {
        return GenerationStep.Decoration.SURFACE_STRUCTURES;
    }

    private static boolean isFeatureChunk(Structure.GenerationContext context) {
        BlockPos blockPos = context.chunkPos().getWorldPosition();
        int landHeight = context.chunkGenerator().getFirstOccupiedHeight(
                blockPos.getX(), blockPos.getZ(),
                Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
        NoiseColumn columnOfBlocks = context.chunkGenerator().getBaseColumn(
                blockPos.getX(), blockPos.getZ(), context.heightAccessor(), context.randomState());
        BlockState topBlock = columnOfBlocks.getBlock(landHeight);
        return topBlock.getFluidState().isEmpty() && landHeight < 200;
    }

    @Override
    public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext context) {
        if (allowCave) {
            BlockPos.MutableBlockPos result = isFeatureChunkCave(context);
            if (result == null) {
                return Optional.empty();
            }
            LOGGER.debug("Found cave colony location at {}", result);
        }

        if (!allowCave && !isFeatureChunk(context)) {
            return Optional.empty();
        }

        // Use the chunk position as the generation point with an empty stub
        // The actual building placement is handled by the colony system, not vanilla worldgen
        BlockPos blockpos = context.chunkPos().getMiddleBlockPosition(0);
        int topY = context.chunkGenerator().getFirstFreeHeight(
                blockpos.getX(), blockpos.getZ(),
                Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
        BlockPos finalPos = blockpos.above(topY);
        Optional<Structure.GenerationStub> gen = Optional.of(
                new Structure.GenerationStub(finalPos, builder -> {}));
        if (gen.isPresent()) {
            LOGGER.debug("New colony at chunk {}", context.chunkPos());
        }
        return gen;
    }

    private static BlockPos.MutableBlockPos isFeatureChunkCave(GenerationContext context) {
        BlockPos blockPos = context.chunkPos().getWorldPosition();
        ChunkPos chunkPos = new ChunkPos(blockPos);
        int currentY = 0;
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        for (int i = 0; i < 10; i++) {
            currentY += context.random().nextInt(0, 30);
            for (int curChunkX = chunkPos.x - 1; curChunkX <= chunkPos.x + 1; curChunkX++) {
                for (int curChunkZ = chunkPos.z - 1; curChunkZ <= chunkPos.z + 1; curChunkZ++) {
                    mutable.set(curChunkX << 4, currentY, curChunkZ << 4);
                    NoiseColumn blockView = context.chunkGenerator().getBaseColumn(
                            mutable.getX(), mutable.getZ(), context.heightAccessor(), context.randomState());
                    if (blockView.getBlock(mutable.getY()).isAir()) {
                        int airCount = 1;
                        BlockPos.MutableBlockPos probe = new BlockPos.MutableBlockPos(mutable.getX(), mutable.getY(), mutable.getZ());
                        while (probe.getY() > context.chunkGenerator().getMinY()) {
                            if (blockView.getBlock(probe.getY()).isAir()) {
                                airCount++;
                            } else {
                                break;
                            }
                            probe.move(Direction.DOWN);
                        }
                        probe.setY(currentY);
                        while (probe.getY() < context.chunkGenerator().getMinY() + context.chunkGenerator().getGenDepth()) {
                            if (blockView.getBlock(probe.getY()).isAir()) {
                                airCount++;
                                if (airCount >= 32) break;
                            } else {
                                break;
                            }
                            probe.move(Direction.UP);
                        }
                        if (airCount >= 32) {
                            return mutable;
                        }
                    }
                }
            }
        }
        return null;
    }
}
