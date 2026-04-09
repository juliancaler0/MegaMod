package com.ultra.megamod.feature.citizen.blueprint;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityProcessor;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.*;

/**
 * Static utility methods for working with blueprints in the colony system.
 * Provides world-scanning, tile entity instantiation, and helper methods.
 */
public final class BlueprintUtils {

    private static final Logger LOGGER = LogUtils.getLogger();

    private BlueprintUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Creates a Blueprint by scanning a region of the world between two corners (inclusive).
     * Air blocks are stored as palette index 0. Tile entities and entities within the
     * region are captured.
     *
     * @param world   the world to scan
     * @param corner1 first corner (any corner of the bounding box)
     * @param corner2 opposite corner
     * @param name    a name for the blueprint
     * @return the scanned Blueprint
     */
    public static Blueprint createBlueprint(Level world, BlockPos corner1, BlockPos corner2, String name) {
        return createBlueprint(world, corner1, corner2, name, true);
    }

    /**
     * Creates a Blueprint by scanning a region of the world between two corners (inclusive).
     *
     * @param world        the world to scan
     * @param corner1      first corner
     * @param corner2      opposite corner
     * @param name         a name for the blueprint
     * @param saveEntities whether to include non-block entities
     * @return the scanned Blueprint
     */
    public static Blueprint createBlueprint(Level world, BlockPos corner1, BlockPos corner2,
                                            String name, boolean saveEntities) {
        // Normalize corners to min/max
        BlockPos minPos = new BlockPos(
                Math.min(corner1.getX(), corner2.getX()),
                Math.min(corner1.getY(), corner2.getY()),
                Math.min(corner1.getZ(), corner2.getZ()));
        BlockPos maxPos = new BlockPos(
                Math.max(corner1.getX(), corner2.getX()),
                Math.max(corner1.getY(), corner2.getY()),
                Math.max(corner1.getZ(), corner2.getZ()));

        short sizeX = (short) (maxPos.getX() - minPos.getX() + 1);
        short sizeY = (short) (maxPos.getY() - minPos.getY() + 1);
        short sizeZ = (short) (maxPos.getZ() - minPos.getZ() + 1);

        List<BlockState> palette = new ArrayList<>();
        palette.add(Blocks.AIR.defaultBlockState()); // Index 0 is always AIR

        short[][][] structure = new short[sizeY][sizeZ][sizeX];
        List<CompoundTag> tileEntitiesList = new ArrayList<>();
        List<String> requiredMods = new ArrayList<>();

        for (BlockPos mutablePos : BlockPos.betweenClosed(minPos, maxPos)) {
            BlockState state = world.getBlockState(mutablePos);

            short x = (short) (mutablePos.getX() - minPos.getX());
            short y = (short) (mutablePos.getY() - minPos.getY());
            short z = (short) (mutablePos.getZ() - minPos.getZ());

            // Track required mods
            Identifier blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock());
            String modNamespace = blockId.getNamespace();
            if (!modNamespace.equals("minecraft") && !modNamespace.equals("megamod")
                    && !requiredMods.contains(modNamespace)) {
                requiredMods.add(modNamespace);
            }

            // Capture tile entity data
            LevelChunk chunk = world.getChunkAt(mutablePos);
            BlockEntity te = chunk.getBlockEntity(mutablePos);
            if (te != null && !te.isRemoved()) {
                CompoundTag teTag = te.saveWithFullMetadata(world.registryAccess());
                teTag.putShort("x", x);
                teTag.putShort("y", y);
                teTag.putShort("z", z);
                tileEntitiesList.add(teTag);
            }

            // Add to palette if needed
            if (!palette.contains(state)) {
                palette.add(state);
            }
            structure[y][z][x] = (short) palette.indexOf(state);
        }

        CompoundTag[] tileEntities = tileEntitiesList.toArray(new CompoundTag[0]);

        // Capture entities
        List<CompoundTag> entityTags = new ArrayList<>();
        if (saveEntities) {
            List<Entity> entities = world.getEntities(null, new AABB(
                    minPos.getX(), minPos.getY(), minPos.getZ(),
                    maxPos.getX() + 1.0, maxPos.getY() + 1.0, maxPos.getZ() + 1.0));

            for (Entity entity : entities) {
                if (entity.isRemoved()) continue;

                Vec3 oldPos = entity.position();
                TagValueOutput valueOutput = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, world.registryAccess());
                entity.save(valueOutput);
                CompoundTag entityTag = valueOutput.buildResult();

                // Store relative position
                ListTag posList = new ListTag();
                posList.add(DoubleTag.valueOf(oldPos.x - minPos.getX()));
                posList.add(DoubleTag.valueOf(oldPos.y - minPos.getY()));
                posList.add(DoubleTag.valueOf(oldPos.z - minPos.getZ()));
                entityTag.put("Pos", posList);

                BlockPos entityBlockPos = entity instanceof HangingEntity hang
                        ? hang.getPos() : entity.blockPosition();
                entityTag.putInt("TileX", entityBlockPos.getX() - minPos.getX());
                entityTag.putInt("TileY", entityBlockPos.getY() - minPos.getY());
                entityTag.putInt("TileZ", entityBlockPos.getZ() - minPos.getZ());

                entityTags.add(entityTag);
            }
        }

        Blueprint blueprint = new Blueprint(sizeX, sizeY, sizeZ, (short) palette.size(),
                palette, structure, tileEntities, requiredMods);
        blueprint.setEntities(entityTags.toArray(new CompoundTag[0]));

        if (name != null) {
            blueprint.setName(name);
        }

        return blueprint;
    }

    /**
     * Attempts to instantiate a BlockEntity from a BlockInfo's tile entity data.
     * The block entity is created at the BlockInfo's position with its stored NBT.
     *
     * @param info  the block info containing state and tile entity data
     * @param level the world to associate with the block entity (may be null for clientside preview)
     * @return the instantiated BlockEntity, or empty if creation fails
     */
    public static Optional<BlockEntity> instantiateTileEntity(BlockInfo info, @Nullable Level level) {
        if (info == null || !info.hasTileEntityData() || info.state() == null) {
            return Optional.empty();
        }

        try {
            CompoundTag compound = info.tileEntityData().copy();
            compound.putInt("x", info.pos().getX());
            compound.putInt("y", info.pos().getY());
            compound.putInt("z", info.pos().getZ());

            BlockEntity entity = level != null
                    ? BlockEntity.loadStatic(info.pos(), info.state(), compound, level.registryAccess())
                    : null;
            if (entity != null) {
                if (!entity.getType().isValid(info.state())) {
                    LOGGER.warn("Tile entity {} does not accept block state: {}",
                            compound.getStringOr("id", "unknown"), info.state());
                    return Optional.empty();
                }
                entity.setLevel(level);
                return Optional.of(entity);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to instantiate tile entity at {}: {}",
                    info.pos(), info.tileEntityData().getStringOr("id", "unknown"), e);
        }

        return Optional.empty();
    }

    /**
     * Instantiates all tile entities in a blueprint and returns them as a map.
     *
     * @param blueprint the blueprint
     * @param level     the world to use for construction
     * @return map of local position to instantiated BlockEntity
     */
    public static Map<BlockPos, BlockEntity> instantiateAllTileEntities(Blueprint blueprint, @Nullable Level level) {
        Map<BlockPos, BlockEntity> result = new HashMap<>();
        for (BlockInfo info : blueprint.getBlockInfoAsList()) {
            if (info.hasTileEntityData()) {
                instantiateTileEntity(info, level).ifPresent(be -> result.put(info.pos(), be));
            }
        }
        return result;
    }

    /**
     * Instantiates entities from a blueprint's entity data.
     *
     * @param blueprint the blueprint
     * @param level     the world to use for entity construction
     * @return list of instantiated entities
     */
    public static List<Entity> instantiateEntities(Blueprint blueprint, Level level) {
        List<Entity> result = new ArrayList<>();
        for (CompoundTag entityTag : blueprint.getEntities()) {
            if (entityTag == null) continue;

            try {
                CompoundTag compound = entityTag.copy();
                // Assign a new UUID to avoid duplicates
                compound.putString("UUID", UUID.randomUUID().toString());

                Entity entity = EntityType.loadEntityRecursive(compound, level, EntitySpawnReason.LOAD, EntityProcessor.NOP);
                if (entity != null) {
                    entity.setOldPosAndRot();
                    result.add(entity);
                }
            } catch (Exception e) {
                LOGGER.error("Failed to instantiate entity from blueprint NBT", e);
            }
        }
        return result;
    }

    /**
     * Counts non-air blocks in a blueprint.
     */
    public static int countNonAirBlocks(Blueprint blueprint) {
        int count = 0;
        for (BlockInfo info : blueprint.getBlockInfoAsList()) {
            if (info.state() != null && !info.state().isAir()) {
                count++;
            }
        }
        return count;
    }

    /**
     * Collects a material list (block state to count mapping) from a blueprint.
     */
    public static Map<BlockState, Integer> getMaterialList(Blueprint blueprint) {
        Map<BlockState, Integer> materials = new HashMap<>();
        for (BlockInfo info : blueprint.getBlockInfoAsList()) {
            if (info.state() != null && !info.state().isAir()) {
                materials.merge(info.state(), 1, Integer::sum);
            }
        }
        return materials;
    }
}
