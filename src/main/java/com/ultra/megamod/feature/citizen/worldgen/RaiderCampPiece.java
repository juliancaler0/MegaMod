package com.ultra.megamod.feature.citizen.worldgen;

import com.ultra.megamod.feature.citizen.raid.AbstractRaiderEntity;
import com.ultra.megamod.feature.citizen.raid.EntityBarbarian;
import com.ultra.megamod.feature.citizen.raid.EntityPirate;
import com.ultra.megamod.feature.citizen.raid.EntityMummy;
import com.ultra.megamod.feature.citizen.raid.EntityShieldmaiden;
import com.ultra.megamod.feature.citizen.raid.EntityAmazonSpearman;
import com.ultra.megamod.feature.citizen.raid.RaiderCulture;
import com.ultra.megamod.feature.citizen.raid.RaiderEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;

/**
 * StructurePiece for raider camp generation.
 * Places a small raider camp (campfire, log seats, tent-like structures)
 * with 3-5 raiders of a biome-appropriate culture.
 */
public class RaiderCampPiece extends StructurePiece {

    private final RaiderCulture culture;
    private final int raiderCount;

    public RaiderCampPiece(RaiderCulture culture, int raiderCount, BlockPos center, RandomSource random) {
        super(ColonyWorldGenRegistry.RAIDER_CAMP_PIECE_TYPE.get(),
                0, makeBoundingBox(center));
        this.culture = culture;
        this.raiderCount = raiderCount;
        this.setOrientation(Direction.Plane.HORIZONTAL.getRandomDirection(random));
    }

    public RaiderCampPiece(StructurePieceSerializationContext context, CompoundTag tag) {
        super(ColonyWorldGenRegistry.RAIDER_CAMP_PIECE_TYPE.get(), tag);
        this.culture = RaiderCulture.fromOrdinal(tag.getIntOr("Culture", 0));
        this.raiderCount = tag.getIntOr("RaiderCount", 3);
    }

    private static BoundingBox makeBoundingBox(BlockPos center) {
        return new BoundingBox(
                center.getX() - 5, center.getY() - 2, center.getZ() - 5,
                center.getX() + 5, center.getY() + 6, center.getZ() + 5
        );
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putInt("Culture", culture.ordinal());
        tag.putInt("RaiderCount", raiderCount);
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager,
                            ChunkGenerator generator, RandomSource random,
                            BoundingBox box, ChunkPos chunkPos, BlockPos pivot) {
        int centerX = (this.boundingBox.minX() + this.boundingBox.maxX()) / 2;
        int centerZ = (this.boundingBox.minZ() + this.boundingBox.maxZ()) / 2;
        int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, centerX, centerZ);

        BlockPos campCenter = new BlockPos(centerX, surfaceY, centerZ);

        // Place campfire at center
        if (box.isInside(campCenter)) {
            BlockState campfire = Blocks.CAMPFIRE.defaultBlockState()
                    .setValue(CampfireBlock.LIT, true);
            level.setBlock(campCenter, campfire, 2);
        }

        // Place log seats around campfire
        BlockPos[] seatOffsets = {
                campCenter.offset(2, 0, 0),
                campCenter.offset(-2, 0, 0),
                campCenter.offset(0, 0, 2),
                campCenter.offset(0, 0, -2)
        };
        for (BlockPos seatPos : seatOffsets) {
            if (box.isInside(seatPos)) {
                BlockState seatState = getCultureSeatBlock();
                level.setBlock(seatPos, seatState, 2);
            }
        }

        // Place tent poles (fence posts at corners)
        BlockPos[] poleOffsets = {
                campCenter.offset(4, 0, 4),
                campCenter.offset(-4, 0, 4),
                campCenter.offset(4, 0, -4),
                campCenter.offset(-4, 0, -4)
        };
        for (BlockPos polePos : poleOffsets) {
            BlockPos adjustedPole = new BlockPos(polePos.getX(),
                    level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, polePos.getX(), polePos.getZ()),
                    polePos.getZ());
            if (box.isInside(adjustedPole)) {
                level.setBlock(adjustedPole, Blocks.OAK_FENCE.defaultBlockState(), 2);
                level.setBlock(adjustedPole.above(), Blocks.OAK_FENCE.defaultBlockState(), 2);
            }
        }

        // Place a weapon rack / barrel
        BlockPos barrelPos = campCenter.offset(1, 0, 1);
        if (box.isInside(barrelPos)) {
            level.setBlock(barrelPos, Blocks.BARREL.defaultBlockState(), 2);
        }

        // Spawn raiders (deferred — only if this is a ServerLevel)
        if (level instanceof ServerLevel serverLevel) {
            for (int i = 0; i < raiderCount; i++) {
                double angle = (2.0 * Math.PI / raiderCount) * i;
                int rx = centerX + (int) (3.0 * Math.cos(angle));
                int rz = centerZ + (int) (3.0 * Math.sin(angle));
                int ry = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, rx, rz);
                BlockPos spawnPos = new BlockPos(rx, ry, rz);

                if (!box.isInside(spawnPos)) continue;

                EntityType<? extends Monster> raiderType = getBasicRaiderType();
                try {
                    Monster raider = raiderType.create(serverLevel, EntitySpawnReason.STRUCTURE);
                    if (raider instanceof AbstractRaiderEntity abstractRaider) {
                        abstractRaider.setCulture(culture);
                        abstractRaider.setTargetPos(campCenter);
                    }
                    if (raider != null) {
                        raider.setPos(rx + 0.5, ry, rz + 0.5);
                        raider.setYRot(random.nextFloat() * 360.0f);
                        raider.setPersistenceRequired();
                        serverLevel.addFreshEntity(raider);
                    }
                } catch (Exception ignored) {
                    // Silently skip if entity creation fails
                }
            }
        }
    }

    /**
     * Returns the basic (melee) raider entity type for this camp's culture.
     */
    @SuppressWarnings("unchecked")
    private EntityType<? extends Monster> getBasicRaiderType() {
        return switch (culture) {
            case BARBARIAN -> (EntityType<? extends Monster>) (EntityType<?>) RaiderEntityRegistry.BARBARIAN.get();
            case PIRATE -> (EntityType<? extends Monster>) (EntityType<?>) RaiderEntityRegistry.PIRATE.get();
            case EGYPTIAN -> (EntityType<? extends Monster>) (EntityType<?>) RaiderEntityRegistry.MUMMY.get();
            case NORSEMEN -> (EntityType<? extends Monster>) (EntityType<?>) RaiderEntityRegistry.SHIELDMAIDEN.get();
            case AMAZON -> (EntityType<? extends Monster>) (EntityType<?>) RaiderEntityRegistry.AMAZON_SPEARMAN.get();
            case DROWNED_PIRATE -> (EntityType<? extends Monster>) (EntityType<?>) RaiderEntityRegistry.DROWNED_PIRATE.get();
        };
    }

    /**
     * Returns a culture-appropriate seat block (log type).
     */
    private BlockState getCultureSeatBlock() {
        return switch (culture) {
            case BARBARIAN -> Blocks.SPRUCE_LOG.defaultBlockState();
            case PIRATE -> Blocks.DARK_OAK_LOG.defaultBlockState();
            case EGYPTIAN -> Blocks.SANDSTONE.defaultBlockState();
            case NORSEMEN -> Blocks.BIRCH_LOG.defaultBlockState();
            case AMAZON -> Blocks.JUNGLE_LOG.defaultBlockState();
            case DROWNED_PIRATE -> Blocks.DARK_OAK_LOG.defaultBlockState();
        };
    }
}
