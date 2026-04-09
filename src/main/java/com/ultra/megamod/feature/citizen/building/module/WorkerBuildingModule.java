package com.ultra.megamod.feature.citizen.building.module;

import com.ultra.megamod.feature.citizen.data.CitizenJob;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Standard implementation of {@link IAssignsJob} for worker buildings.
 * Manages a list of assigned citizen workers up to a configurable maximum,
 * with full NBT persistence.
 */
public class WorkerBuildingModule implements IAssignsJob, IPersistentModule {

    private static final String NBT_JOB_TYPE = "JobType";
    private static final String NBT_MAX_WORKERS = "MaxWorkers";
    private static final String NBT_WORKERS = "Workers";

    private final CitizenJob jobType;
    private int maxWorkers;
    private final List<UUID> assignedWorkerIds;

    /**
     * Creates a new worker building module.
     *
     * @param jobType    the job type assigned to workers at this building
     * @param maxWorkers the maximum number of workers
     */
    public WorkerBuildingModule(CitizenJob jobType, int maxWorkers) {
        this.jobType = jobType;
        this.maxWorkers = maxWorkers;
        this.assignedWorkerIds = new ArrayList<>();
    }

    @Override
    public String getModuleId() {
        return "worker";
    }

    @Override
    public CitizenJob getJobType() {
        return jobType;
    }

    @Override
    public int getMaxWorkers() {
        return maxWorkers;
    }

    /**
     * Updates the maximum number of workers this building can support.
     * Does not evict existing workers if the new max is lower.
     *
     * @param maxWorkers the new max worker count
     */
    public void setMaxWorkers(int maxWorkers) {
        this.maxWorkers = maxWorkers;
    }

    @Override
    public List<UUID> getAssignedWorkers() {
        return Collections.unmodifiableList(assignedWorkerIds);
    }

    @Override
    public boolean assignWorker(UUID citizenId) {
        if (citizenId == null) {
            return false;
        }
        if (assignedWorkerIds.size() >= maxWorkers) {
            return false;
        }
        if (assignedWorkerIds.contains(citizenId)) {
            return false;
        }
        assignedWorkerIds.add(citizenId);
        return true;
    }

    @Override
    public void removeWorker(UUID citizenId) {
        assignedWorkerIds.remove(citizenId);
    }

    @Override
    public void onBuildingLoad(CompoundTag tag) {
        CompoundTag moduleTag = tag.getCompoundOrEmpty(getModuleId());
        maxWorkers = moduleTag.getIntOr(NBT_MAX_WORKERS, maxWorkers);

        assignedWorkerIds.clear();
        if (moduleTag.contains(NBT_WORKERS)) {
            ListTag workerList = moduleTag.getListOrEmpty(NBT_WORKERS);
            for (int i = 0; i < workerList.size(); i++) {
                Tag entry = workerList.get(i);
                if (entry instanceof StringTag stringTag) {
                    try {
                        assignedWorkerIds.add(UUID.fromString(stringTag.value()));
                    } catch (IllegalArgumentException ignored) {
                        // Skip invalid UUIDs
                    }
                }
            }
        }
    }

    @Override
    public void onBuildingSave(CompoundTag tag) {
        CompoundTag moduleTag = new CompoundTag();
        moduleTag.putString(NBT_JOB_TYPE, jobType.name());
        moduleTag.putInt(NBT_MAX_WORKERS, maxWorkers);

        ListTag workerList = new ListTag();
        for (UUID id : assignedWorkerIds) {
            workerList.add(StringTag.valueOf(id.toString()));
        }
        moduleTag.put(NBT_WORKERS, workerList);

        tag.put(getModuleId(), moduleTag);
    }

    @Override
    public void onBuildingTick(Level level) {
        // Worker buildings don't need per-tick logic by default.
        // Subclasses or composing buildings can override behavior.
    }

    /**
     * Returns true if this building has open worker slots.
     *
     * @return true if workers can still be assigned
     */
    public boolean hasOpenSlots() {
        return assignedWorkerIds.size() < maxWorkers;
    }

    /**
     * Returns the number of currently assigned workers.
     *
     * @return the assigned worker count
     */
    public int getWorkerCount() {
        return assignedWorkerIds.size();
    }
}
