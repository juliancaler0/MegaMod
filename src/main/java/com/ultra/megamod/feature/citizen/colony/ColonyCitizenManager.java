package com.ultra.megamod.feature.citizen.colony;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Tracks all citizens belonging to a colony. Max citizens is based on housing capacity
 * (bed count from Residence buildings, upgradable via Town Hall level).
 * <p>
 * Modeled after MineColonies' ICitizenManager.
 * The actual citizen entities are managed by {@link com.ultra.megamod.feature.citizen.CitizenManager};
 * this class is the colony-level index that tracks which citizen UUIDs belong to this colony
 * and what their current assignment / home is.
 */
public class ColonyCitizenManager {

    // ==================== CitizenEntry ====================

    /**
     * Colony-level tracking data for one citizen.
     */
    public static class CitizenEntry {
        private final UUID entityUuid;
        private final int citizenId;
        private String name;
        private String job;
        private BlockPos homePos;
        private BlockPos workPos;

        public CitizenEntry(UUID entityUuid, int citizenId, String name, String job) {
            this.entityUuid = entityUuid;
            this.citizenId = citizenId;
            this.name = name;
            this.job = job;
        }

        public UUID getEntityUuid() { return entityUuid; }
        public int getCitizenId() { return citizenId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getJob() { return job; }
        public void setJob(String job) { this.job = job; }
        @Nullable public BlockPos getHomePos() { return homePos; }
        public void setHomePos(@Nullable BlockPos pos) { this.homePos = pos; }
        @Nullable public BlockPos getWorkPos() { return workPos; }
        public void setWorkPos(@Nullable BlockPos pos) { this.workPos = pos; }
    }

    // ==================== Fields ====================

    private final Map<Integer, CitizenEntry> citizens = new LinkedHashMap<>();
    private final Map<UUID, Integer> uuidToId = new HashMap<>();
    private int nextCitizenId = 1;
    private int maxCitizens = 4; // Base capacity, increases with housing
    private boolean dirty = false;

    // ==================== Citizen Management ====================

    /**
     * Registers a new citizen in this colony.
     *
     * @param entityUuid the entity UUID of the citizen
     * @param name       citizen name
     * @param job        job type string
     * @return the assigned citizen ID, or -1 if at capacity
     */
    public int addCitizen(@NotNull UUID entityUuid, @NotNull String name, @NotNull String job) {
        if (uuidToId.containsKey(entityUuid)) {
            return uuidToId.get(entityUuid); // Already registered
        }
        int id = nextCitizenId++;
        CitizenEntry entry = new CitizenEntry(entityUuid, id, name, job);
        citizens.put(id, entry);
        uuidToId.put(entityUuid, id);
        dirty = true;
        return id;
    }

    /**
     * Removes a citizen by their colony-internal ID.
     *
     * @param citizenId the citizen's colony ID
     * @return true if removed
     */
    public boolean removeCitizen(int citizenId) {
        CitizenEntry removed = citizens.remove(citizenId);
        if (removed != null) {
            uuidToId.remove(removed.getEntityUuid());
            dirty = true;
            return true;
        }
        return false;
    }

    /**
     * Removes a citizen by entity UUID.
     */
    public boolean removeCitizenByUuid(@NotNull UUID entityUuid) {
        Integer id = uuidToId.get(entityUuid);
        if (id != null) {
            return removeCitizen(id);
        }
        return false;
    }

    /**
     * Gets a citizen by colony-internal ID.
     */
    @Nullable
    public CitizenEntry getCitizen(int citizenId) {
        return citizens.get(citizenId);
    }

    /**
     * Gets a citizen by entity UUID.
     */
    @Nullable
    public CitizenEntry getCitizenByUuid(@NotNull UUID entityUuid) {
        Integer id = uuidToId.get(entityUuid);
        return id != null ? citizens.get(id) : null;
    }

    /**
     * Returns an unmodifiable view of all citizens.
     */
    @NotNull
    public Map<Integer, CitizenEntry> getCitizens() {
        return Collections.unmodifiableMap(citizens);
    }

    /**
     * Current number of citizens in the colony.
     */
    public int getCitizenCount() {
        return citizens.size();
    }

    /**
     * Maximum number of citizens the colony can support.
     */
    public int getMaxCitizens() {
        return maxCitizens;
    }

    /**
     * Sets the max citizens (updated when housing changes).
     */
    public void setMaxCitizens(int max) {
        this.maxCitizens = max;
        dirty = true;
    }

    /**
     * Whether the colony is at citizen capacity.
     */
    public boolean isAtCapacity() {
        return citizens.size() >= maxCitizens;
    }

    /**
     * Returns all citizens with a specific job type.
     */
    @NotNull
    public List<CitizenEntry> getCitizensByJob(@NotNull String job) {
        List<CitizenEntry> result = new ArrayList<>();
        for (CitizenEntry entry : citizens.values()) {
            if (entry.getJob().equals(job)) {
                result.add(entry);
            }
        }
        return result;
    }

    // ==================== Dirty Flag ====================

    public boolean isDirty() { return dirty; }
    public void clearDirty() { dirty = false; }
    public void markDirty() { dirty = true; }

    // ==================== NBT ====================

    private static final String TAG_CITIZENS = "citizens";
    private static final String TAG_NEXT_ID = "nextCitizenId";
    private static final String TAG_MAX = "maxCitizens";
    private static final String TAG_UUID = "uuid";
    private static final String TAG_CIT_ID = "citizenId";
    private static final String TAG_NAME = "name";
    private static final String TAG_JOB = "job";
    private static final String TAG_HOME_X = "homeX";
    private static final String TAG_HOME_Y = "homeY";
    private static final String TAG_HOME_Z = "homeZ";
    private static final String TAG_WORK_X = "workX";
    private static final String TAG_WORK_Y = "workY";
    private static final String TAG_WORK_Z = "workZ";

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt(TAG_NEXT_ID, nextCitizenId);
        tag.putInt(TAG_MAX, maxCitizens);

        ListTag list = new ListTag();
        for (CitizenEntry entry : citizens.values()) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putString(TAG_UUID, entry.getEntityUuid().toString());
            entryTag.putInt(TAG_CIT_ID, entry.getCitizenId());
            entryTag.putString(TAG_NAME, entry.getName());
            entryTag.putString(TAG_JOB, entry.getJob());
            if (entry.getHomePos() != null) {
                entryTag.putInt(TAG_HOME_X, entry.getHomePos().getX());
                entryTag.putInt(TAG_HOME_Y, entry.getHomePos().getY());
                entryTag.putInt(TAG_HOME_Z, entry.getHomePos().getZ());
            }
            if (entry.getWorkPos() != null) {
                entryTag.putInt(TAG_WORK_X, entry.getWorkPos().getX());
                entryTag.putInt(TAG_WORK_Y, entry.getWorkPos().getY());
                entryTag.putInt(TAG_WORK_Z, entry.getWorkPos().getZ());
            }
            list.add(entryTag);
        }
        tag.put(TAG_CITIZENS, list);
        return tag;
    }

    public void load(CompoundTag tag) {
        citizens.clear();
        uuidToId.clear();
        nextCitizenId = tag.getIntOr(TAG_NEXT_ID, 1);
        maxCitizens = tag.getIntOr(TAG_MAX, 4);

        if (tag.contains(TAG_CITIZENS)) {
            ListTag list = tag.getListOrEmpty(TAG_CITIZENS);
            for (int i = 0; i < list.size(); i++) {
                if (!(list.get(i) instanceof CompoundTag entryTag)) continue;
                String uuidStr = entryTag.getStringOr(TAG_UUID, "");
                UUID uuid = uuidStr.isEmpty() ? null : UUID.fromString(uuidStr);
                if (uuid == null) continue;
                int citId = entryTag.getIntOr(TAG_CIT_ID, nextCitizenId++);
                String name = entryTag.getStringOr(TAG_NAME, "Citizen");
                String job = entryTag.getStringOr(TAG_JOB, "UNEMPLOYED");
                CitizenEntry entry = new CitizenEntry(uuid, citId, name, job);

                if (entryTag.contains(TAG_HOME_X)) {
                    entry.setHomePos(new BlockPos(
                            entryTag.getIntOr(TAG_HOME_X, 0),
                            entryTag.getIntOr(TAG_HOME_Y, 0),
                            entryTag.getIntOr(TAG_HOME_Z, 0)
                    ));
                }
                if (entryTag.contains(TAG_WORK_X)) {
                    entry.setWorkPos(new BlockPos(
                            entryTag.getIntOr(TAG_WORK_X, 0),
                            entryTag.getIntOr(TAG_WORK_Y, 0),
                            entryTag.getIntOr(TAG_WORK_Z, 0)
                    ));
                }

                citizens.put(citId, entry);
                uuidToId.put(uuid, citId);
                if (citId >= nextCitizenId) {
                    nextCitizenId = citId + 1;
                }
            }
        }
        dirty = false;
    }
}
