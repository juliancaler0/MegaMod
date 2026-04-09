package com.ultra.megamod.feature.citizen.blueprint;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.*;

/**
 * Core blueprint class for the colony building system.
 * Stores a structure as a 3D array of palette indices with optional tile entity data.
 * <p>
 * Based on Structurize's Blueprint format, adapted for MegaMod's colony system.
 * Built alongside (not replacing) the existing {@code com.ultra.megamod.feature.schematic} system.
 * <p>
 * Coordinate order in the structure array is [y][z][x].
 */
public class Blueprint {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String ENTITY_POS = "Pos";

    // --- Dimensions ---
    private short sizeX;
    private short sizeY;
    private short sizeZ;

    // --- Block data ---
    /** Block state palette. Index 0 is always AIR. */
    private List<BlockState> palette;
    private short paletteSize;

    /** 3D structure array indexed [y][z][x], storing palette indices. */
    private short[][][] structure;

    /** 3D array of block entity NBT data, aligned with structure indices. */
    private CompoundTag[][][] tileEntities;

    /** Serialized entity NBT (non-block entities within the structure). */
    private CompoundTag[] entities = new CompoundTag[0];

    // --- Metadata ---
    private List<String> requiredMods;
    private String name;
    private String fileName;
    private Path filePath;
    private String packName;
    private String[] architects;

    // --- Caches ---
    private List<BlockInfo> cacheBlockInfo = null;
    private Map<BlockPos, BlockInfo> cacheBlockInfoMap = null;
    private BlockPos cachePrimaryOffset = null;

    /** Current rotation/mirror state relative to the freshly loaded file. */
    private RotationMirror rotationMirror = RotationMirror.NONE;

    /**
     * Full constructor used when loading from NBT.
     *
     * @param sizeX        X dimension
     * @param sizeY        Y dimension
     * @param sizeZ        Z dimension
     * @param paletteSize  number of entries in the palette
     * @param palette      the block state palette
     * @param structure    3D structure array [y][z][x]
     * @param tileEntities flat array of tile entity CompoundTags with x/y/z short keys
     * @param requiredMods list of required mod IDs
     */
    public Blueprint(short sizeX, short sizeY, short sizeZ, short paletteSize,
                     List<BlockState> palette, short[][][] structure,
                     CompoundTag[] tileEntities, List<String> requiredMods) {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
        this.paletteSize = paletteSize;
        this.palette = new ArrayList<>(palette);
        this.structure = structure;
        this.tileEntities = new CompoundTag[sizeY][sizeZ][sizeX];
        this.requiredMods = new ArrayList<>(requiredMods);

        // Place tile entities into the 3D array at their stored local positions
        for (CompoundTag te : tileEntities) {
            if (te != null) {
                int tx = te.getShortOr("x", (short) 0);
                int ty = te.getShortOr("y", (short) 0);
                int tz = te.getShortOr("z", (short) 0);
                if (ty >= 0 && ty < sizeY && tz >= 0 && tz < sizeZ && tx >= 0 && tx < sizeX) {
                    this.tileEntities[ty][tz][tx] = te;
                }
            }
        }
    }

    /**
     * Simple constructor for creating an empty blueprint of the given dimensions.
     * Index 0 of the palette is set to AIR.
     */
    public Blueprint(short sizeX, short sizeY, short sizeZ) {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
        this.structure = new short[sizeY][sizeZ][sizeX];
        this.tileEntities = new CompoundTag[sizeY][sizeZ][sizeX];
        this.requiredMods = new ArrayList<>();
        this.palette = new ArrayList<>();
        this.palette.add(Blocks.AIR.defaultBlockState());
        this.paletteSize = 1;
    }

    // ========== Dimension getters ==========

    public short getSizeX() { return sizeX; }
    public short getSizeY() { return sizeY; }
    public short getSizeZ() { return sizeZ; }

    // ========== Palette ==========

    public short getPaletteSize() { return paletteSize; }

    /** Returns the palette as an array (without rotation/mirror applied). */
    public BlockState[] getPalette() {
        return palette.toArray(new BlockState[0]);
    }

    // ========== Raw data access ==========

    /** Returns the raw structure array [y][z][x] of palette indices. */
    public short[][][] getStructure() { return structure; }

    /** Returns the raw 3D tile entity array. */
    public CompoundTag[][][] getTileEntities() { return tileEntities; }

    /** Returns the entity NBT array. */
    public CompoundTag[] getEntities() { return entities; }

    public void setEntities(CompoundTag[] entities) {
        this.entities = entities != null ? entities : new CompoundTag[0];
    }

    // ========== Block state access ==========

    /**
     * Gets the block state at the given local position from the palette.
     *
     * @return the BlockState, or AIR if out of bounds
     */
    public BlockState getBlockState(int x, int y, int z) {
        if (x < 0 || x >= sizeX || y < 0 || y >= sizeY || z < 0 || z >= sizeZ) {
            return Blocks.AIR.defaultBlockState();
        }
        int index = structure[y][z][x] & 0xFFFF;
        if (index >= palette.size()) {
            return Blocks.AIR.defaultBlockState();
        }
        return palette.get(index);
    }

    /**
     * Sets the block state at the given local position.
     * Adds the state to the palette if it is not already present.
     */
    public void setBlockState(int x, int y, int z, BlockState state) {
        if (x < 0 || x >= sizeX || y < 0 || y >= sizeY || z < 0 || z >= sizeZ) {
            return;
        }

        int index = palette.indexOf(state);
        if (index == -1) {
            index = paletteSize;
            paletteSize++;
            palette.add(state);
        }

        structure[y][z][x] = (short) index;
        cacheReset(true);
    }

    // ========== Block info ==========

    /**
     * Returns a list of all block infos in the blueprint.
     * The list is cached and rebuilt only when the structure changes.
     */
    public List<BlockInfo> getBlockInfoAsList() {
        if (cacheBlockInfo == null) {
            buildBlockInfoCache();
        }
        return cacheBlockInfo;
    }

    /**
     * Returns a map of position to block info.
     */
    public Map<BlockPos, BlockInfo> getBlockInfoAsMap() {
        if (cacheBlockInfoMap == null) {
            buildBlockInfoCache();
        }
        return cacheBlockInfoMap;
    }

    private void buildBlockInfoCache() {
        int volume = (int) sizeX * sizeY * sizeZ;
        cacheBlockInfo = new ArrayList<>(volume);
        cacheBlockInfoMap = new HashMap<>(volume);

        for (short y = 0; y < sizeY; y++) {
            for (short z = 0; z < sizeZ; z++) {
                for (short x = 0; x < sizeX; x++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    int paletteIdx = structure[y][z][x] & 0xFFFF;
                    BlockState state = paletteIdx < palette.size()
                            ? palette.get(paletteIdx)
                            : Blocks.AIR.defaultBlockState();
                    CompoundTag teData = tileEntities[y][z][x];
                    BlockInfo info = new BlockInfo(pos, state, teData);
                    cacheBlockInfo.add(info);
                    cacheBlockInfoMap.put(pos, info);
                }
            }
        }
    }

    // ========== Primary offset (anchor point) ==========

    /**
     * Gets the primary block offset (anchor point) of the blueprint.
     * Defaults to the center of the bottom layer if no anchor is found.
     */
    public BlockPos getPrimaryBlockOffset() {
        if (cachePrimaryOffset == null) {
            cachePrimaryOffset = findPrimaryBlockOffset();
        }
        return cachePrimaryOffset;
    }

    public void setCachePrimaryOffset(BlockPos offset) {
        this.cachePrimaryOffset = offset;
    }

    private BlockPos findPrimaryBlockOffset() {
        // Look for blocks with blueprint data tag in their tile entity
        List<BlockInfo> anchors = getBlockInfoAsList().stream()
                .filter(info -> info.hasTileEntityData()
                        && info.tileEntityData().contains(BlueprintTagUtils.TAG_BLUEPRINTDATA))
                .toList();

        if (anchors.size() == 1) {
            return anchors.get(0).pos();
        }

        // Default: center of the bottom layer
        return new BlockPos(sizeX / 2, 0, sizeZ / 2);
    }

    // ========== Rotation / Mirror ==========

    /** Returns the current rotation/mirror state relative to the freshly loaded file. */
    public RotationMirror getRotationMirror() {
        return rotationMirror;
    }

    /**
     * Rotates and mirrors the entire blueprint to exactly match the given RotationMirror state.
     *
     * @param target the target rotation/mirror state
     * @param level  the world (needed for entity deserialization during transforms)
     */
    public void setRotationMirror(RotationMirror target, Level level) {
        RotationMirror diff = this.rotationMirror.calcDifferenceTowards(target);
        applyRotationMirror(diff, level);
    }

    /**
     * Applies a relative rotation/mirror transformation to the blueprint contents.
     */
    private void applyRotationMirror(RotationMirror transform, Level level) {
        if (transform == RotationMirror.NONE) {
            return;
        }

        BlockPos primaryOffset = getPrimaryBlockOffset();

        // Determine new dimensions after rotation
        final short newSizeX, newSizeZ;
        final short newSizeY = sizeY;
        switch (transform.getRotation()) {
            case CLOCKWISE_90, COUNTERCLOCKWISE_90 -> {
                newSizeX = sizeZ;
                newSizeZ = sizeX;
            }
            default -> {
                newSizeX = sizeX;
                newSizeZ = sizeZ;
            }
        }

        short[][][] newStructure = new short[newSizeY][newSizeZ][newSizeX];
        CompoundTag[][][] newTileEntities = new CompoundTag[newSizeY][newSizeZ][newSizeX];
        CompoundTag[] newEntities = new CompoundTag[entities.length];

        // Rotate the palette block states
        List<BlockState> newPalette = new ArrayList<>(palette.size());
        for (BlockState bs : palette) {
            if (transform.isMirrored()) {
                bs = bs.mirror(transform.getMirror());
            }
            newPalette.add(bs.rotate(transform.getRotation()));
        }

        // Calculate offset to keep positions non-negative
        BlockPos extremes = transform.applyToPos(new BlockPos(sizeX, sizeY, sizeZ));
        int minX = extremes.getX() < 0 ? -extremes.getX() - 1 : 0;
        int minY = extremes.getY() < 0 ? -extremes.getY() - 1 : 0;
        int minZ = extremes.getZ() < 0 ? -extremes.getZ() - 1 : 0;

        this.palette = newPalette;

        // Transform all blocks
        for (short x = 0; x < sizeX; x++) {
            for (short y = 0; y < sizeY; y++) {
                for (short z = 0; z < sizeZ; z++) {
                    short value = structure[y][z][x];
                    int stateIdx = value & 0xFFFF;
                    BlockState state = stateIdx < newPalette.size()
                            ? newPalette.get(stateIdx) : Blocks.AIR.defaultBlockState();
                    if (state.getBlock() == Blocks.STRUCTURE_VOID) {
                        continue;
                    }

                    BlockPos tempPos = transform.applyToPos(new BlockPos(x, y, z))
                            .offset(minX, minY, minZ);
                    newStructure[tempPos.getY()][tempPos.getZ()][tempPos.getX()] = value;

                    CompoundTag compound = tileEntities[y][z][x];
                    if (compound != null) {
                        compound.putShort("x", (short) tempPos.getX());
                        compound.putShort("y", (short) tempPos.getY());
                        compound.putShort("z", (short) tempPos.getZ());
                    }
                    newTileEntities[tempPos.getY()][tempPos.getZ()][tempPos.getX()] = compound;
                }
            }
        }

        // Transform entities
        for (int i = 0; i < entities.length; i++) {
            CompoundTag entityTag = entities[i];
            if (entityTag != null) {
                newEntities[i] = transformEntityInfo(entityTag, level,
                        new BlockPos(minX, minY, minZ), transform);
            }
        }

        // Update primary offset
        setCachePrimaryOffset(transform.applyToPos(primaryOffset).offset(minX, minY, minZ));

        // Apply new state
        sizeX = newSizeX;
        sizeY = newSizeY;
        sizeZ = newSizeZ;
        structure = newStructure;
        entities = newEntities;
        tileEntities = newTileEntities;
        rotationMirror = rotationMirror.add(transform);

        cacheReset(false);
    }

    /**
     * Transforms an entity's NBT data by rotation/mirror using pure NBT manipulation.
     * No entity instantiation is needed -- we directly adjust "Pos" and "Rotation" tags.
     */
    @Nullable
    private static CompoundTag transformEntityInfo(CompoundTag entityInfo, Level level,
                                                   BlockPos offset, RotationMirror rm) {
        try {
            CompoundTag result = entityInfo.copy();

            // Read current position from "Pos" list tag [x, y, z]
            ListTag posTag = result.getListOrEmpty("Pos");
            double posX = 0, posY = 0, posZ = 0;
            if (posTag.size() >= 3) {
                posX = posTag.getDoubleOr(0, 0.0);
                posY = posTag.getDoubleOr(1, 0.0);
                posZ = posTag.getDoubleOr(2, 0.0);
            }

            // Apply rotation/mirror transform to the position
            Vec3 entityVec = rm.applyToPos(new Vec3(posX, posY, posZ))
                    .add(Vec3.atLowerCornerOf(offset));

            // Write back transformed position
            ListTag newPosTag = new ListTag();
            newPosTag.add(DoubleTag.valueOf(entityVec.x));
            newPosTag.add(DoubleTag.valueOf(entityVec.y));
            newPosTag.add(DoubleTag.valueOf(entityVec.z));
            result.put("Pos", newPosTag);

            // Read current rotation from "Rotation" list tag [yaw, pitch]
            ListTag rotTag = result.getListOrEmpty("Rotation");
            float yaw = 0f, pitch = 0f;
            if (rotTag.size() >= 2) {
                yaw = rotTag.getFloatOr(0, 0f);
                pitch = rotTag.getFloatOr(1, 0f);
            }

            // Adjust yaw based on rotation
            yaw = adjustYaw(yaw, rm);

            // Write back transformed rotation
            ListTag newRotTag = new ListTag();
            newRotTag.add(FloatTag.valueOf(yaw));
            newRotTag.add(FloatTag.valueOf(pitch));
            result.put("Rotation", newRotTag);

            // Also transform TileX/TileY/TileZ if present (for hanging entities)
            if (result.contains("TileX") && result.contains("TileY") && result.contains("TileZ")) {
                int tileX = result.getIntOr("TileX", 0);
                int tileY = result.getIntOr("TileY", 0);
                int tileZ = result.getIntOr("TileZ", 0);
                BlockPos tilePos = rm.applyToPos(new BlockPos(tileX, tileY, tileZ))
                        .offset(offset);
                result.putInt("TileX", tilePos.getX());
                result.putInt("TileY", tilePos.getY());
                result.putInt("TileZ", tilePos.getZ());
            }

            return result;
        } catch (Exception ex) {
            LOGGER.error("Entity NBT failed to transform", ex);
            return null;
        }
    }

    /**
     * Adjusts entity yaw for the given rotation/mirror transform.
     */
    private static float adjustYaw(float yaw, RotationMirror rm) {
        // Apply mirror first
        if (rm.isMirrored()) {
            yaw = 360f - yaw;
        }
        // Apply rotation
        switch (rm.getRotation()) {
            case CLOCKWISE_90 -> yaw += 90f;
            case CLOCKWISE_180 -> yaw += 180f;
            case COUNTERCLOCKWISE_90 -> yaw += 270f;
            default -> {}
        }
        // Normalize to 0-360
        yaw = yaw % 360f;
        if (yaw < 0) yaw += 360f;
        return yaw;
    }

    // ========== Metadata ==========

    public List<String> getRequiredMods() { return requiredMods; }

    public String getName() { return name; }
    public Blueprint setName(String name) { this.name = name; return this; }

    public String getFileName() { return fileName; }
    public Blueprint setFileName(String fileName) { this.fileName = fileName; return this; }

    public Path getFilePath() { return filePath; }
    public Blueprint setFilePath(Path filePath) { this.filePath = filePath; return this; }

    public String getPackName() { return packName; }
    public Blueprint setPackName(String packName) { this.packName = packName; return this; }

    public String[] getArchitects() { return architects; }
    public void setArchitects(String[] architects) { this.architects = architects; }

    // ========== Entities as list ==========

    /** Returns entities as a mutable list of CompoundTags. */
    public List<CompoundTag> getEntitiesAsList() {
        return new ArrayList<>(Arrays.asList(entities));
    }

    // ========== Cache management ==========

    private void cacheReset(boolean resetPrimaryOffset) {
        cacheBlockInfo = null;
        cacheBlockInfoMap = null;
        if (resetPrimaryOffset) {
            cachePrimaryOffset = null;
        }
    }

    // ========== Utility ==========

    private int getVolume() {
        return (int) sizeX * sizeY * sizeZ;
    }

    @Override
    public int hashCode() {
        int result = 31 + (name == null ? 0 : name.hashCode());
        result = 31 * result + (fileName == null ? 0 : fileName.hashCode());
        result = 31 * result + (packName == null ? 0 : packName.hashCode());
        result = 31 * result + paletteSize;
        result = 31 * result + entities.length;
        result = 31 * result + getVolume();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Blueprint other)) return false;
        return Objects.equals(name, other.name)
                && Objects.equals(fileName, other.fileName)
                && Objects.equals(packName, other.packName)
                && paletteSize == other.paletteSize
                && entities.length == other.entities.length
                && getVolume() == other.getVolume();
    }

    @Override
    public String toString() {
        return "Blueprint[size=[%d, %d, %d], fileName=%s, filePath=%s, packName=%s, name=%s, rotMir=%s]"
                .formatted(sizeX, sizeY, sizeZ, fileName, filePath, packName, name, rotationMirror);
    }
}
