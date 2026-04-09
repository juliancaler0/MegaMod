package com.ultra.megamod.feature.citizen.colony;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Tracks all buildings in a colony, mapping BlockPos to building metadata.
 * <p>
 * Modeled after MineColonies' IRegisteredStructureManager, but simplified for MegaMod.
 * Each building entry stores the position, building type ID, level, and custom name.
 * The actual building logic lives in the hut blocks / TileEntityColonyBuilding;
 * this manager is the colony's index of what buildings exist.
 */
public class ColonyBuildingManager {

    // ==================== BuildingEntry ====================

    /**
     * Lightweight data about a single registered building.
     */
    public static class BuildingEntry {
        private final BlockPos pos;
        private final String buildingId;
        private int level;
        private String customName;

        public BuildingEntry(BlockPos pos, String buildingId, int level, String customName) {
            this.pos = pos;
            this.buildingId = buildingId;
            this.level = level;
            this.customName = customName;
        }

        public BlockPos getPos() { return pos; }
        public String getBuildingId() { return buildingId; }
        public int getLevel() { return level; }
        public void setLevel(int level) { this.level = level; }
        public String getCustomName() { return customName; }
        public void setCustomName(String name) { this.customName = name; }
    }

    // ==================== Fields ====================

    private final Map<BlockPos, BuildingEntry> buildings = new LinkedHashMap<>();
    private boolean dirty = false;

    // ==================== Building Management ====================

    /**
     * Registers a new building at the given position.
     *
     * @param pos        block position of the hut block
     * @param buildingId the building type (e.g., "residence", "baker")
     * @param level      initial building level
     * @return the created BuildingEntry
     */
    @NotNull
    public BuildingEntry addBuilding(@NotNull BlockPos pos, @NotNull String buildingId, int level) {
        BuildingEntry entry = new BuildingEntry(pos, buildingId, level, "");
        buildings.put(pos, entry);
        dirty = true;
        return entry;
    }

    /**
     * Removes a building at the given position.
     *
     * @param pos the position
     * @return true if a building was removed
     */
    public boolean removeBuilding(@NotNull BlockPos pos) {
        boolean removed = buildings.remove(pos) != null;
        if (removed) dirty = true;
        return removed;
    }

    /**
     * Gets a building at the given position.
     *
     * @param pos the position
     * @return the BuildingEntry, or null if none
     */
    @Nullable
    public BuildingEntry getBuilding(@NotNull BlockPos pos) {
        return buildings.get(pos);
    }

    /**
     * Returns an unmodifiable view of all buildings.
     */
    @NotNull
    public Map<BlockPos, BuildingEntry> getBuildings() {
        return Collections.unmodifiableMap(buildings);
    }

    /**
     * Returns all buildings of a specific type.
     *
     * @param buildingId the building type to filter by
     * @return list of matching entries
     */
    @NotNull
    public List<BuildingEntry> getBuildingsByType(@NotNull String buildingId) {
        List<BuildingEntry> result = new ArrayList<>();
        for (BuildingEntry entry : buildings.values()) {
            if (entry.getBuildingId().equals(buildingId)) {
                result.add(entry);
            }
        }
        return result;
    }

    /**
     * Checks if the colony has at least one building of the given type.
     */
    public boolean hasBuilding(@NotNull String buildingId) {
        for (BuildingEntry entry : buildings.values()) {
            if (entry.getBuildingId().equals(buildingId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the total count of buildings.
     */
    public int getBuildingCount() {
        return buildings.size();
    }

    /**
     * Returns the highest level among buildings of a given type.
     */
    public int getMaxBuildingLevel(@NotNull String buildingId) {
        int max = 0;
        for (BuildingEntry entry : buildings.values()) {
            if (entry.getBuildingId().equals(buildingId) && entry.getLevel() > max) {
                max = entry.getLevel();
            }
        }
        return max;
    }

    // ==================== Dirty Flag ====================

    public boolean isDirty() { return dirty; }
    public void clearDirty() { dirty = false; }
    public void markDirty() { dirty = true; }

    // ==================== NBT ====================

    private static final String TAG_BUILDINGS = "buildings";
    private static final String TAG_POS_X = "x";
    private static final String TAG_POS_Y = "y";
    private static final String TAG_POS_Z = "z";
    private static final String TAG_BUILDING_ID = "buildingId";
    private static final String TAG_LEVEL = "level";
    private static final String TAG_NAME = "name";

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        for (BuildingEntry entry : buildings.values()) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putInt(TAG_POS_X, entry.getPos().getX());
            entryTag.putInt(TAG_POS_Y, entry.getPos().getY());
            entryTag.putInt(TAG_POS_Z, entry.getPos().getZ());
            entryTag.putString(TAG_BUILDING_ID, entry.getBuildingId());
            entryTag.putInt(TAG_LEVEL, entry.getLevel());
            entryTag.putString(TAG_NAME, entry.getCustomName());
            list.add(entryTag);
        }
        tag.put(TAG_BUILDINGS, list);
        return tag;
    }

    public void load(CompoundTag tag) {
        buildings.clear();
        if (tag.contains(TAG_BUILDINGS)) {
            ListTag list = tag.getListOrEmpty(TAG_BUILDINGS);
            for (int i = 0; i < list.size(); i++) {
                if (!(list.get(i) instanceof CompoundTag entryTag)) continue;
                BlockPos pos = new BlockPos(
                        entryTag.getIntOr(TAG_POS_X, 0),
                        entryTag.getIntOr(TAG_POS_Y, 0),
                        entryTag.getIntOr(TAG_POS_Z, 0)
                );
                String buildingId = entryTag.getStringOr(TAG_BUILDING_ID, "unknown");
                int level = entryTag.getIntOr(TAG_LEVEL, 1);
                String name = entryTag.getStringOr(TAG_NAME, "");
                buildings.put(pos, new BuildingEntry(pos, buildingId, level, name));
            }
        }
        dirty = false;
    }
}
