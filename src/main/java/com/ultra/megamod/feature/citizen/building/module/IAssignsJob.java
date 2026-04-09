package com.ultra.megamod.feature.citizen.building.module;

import com.ultra.megamod.feature.citizen.data.CitizenJob;

import java.util.List;
import java.util.UUID;

/**
 * Module interface for buildings that assign jobs to citizen workers.
 * A building with this module acts as a workplace where citizens perform
 * their assigned job duties.
 */
public interface IAssignsJob extends IBuildingModule {

    /**
     * Returns the job type this building assigns to its workers.
     *
     * @return the citizen job type
     */
    CitizenJob getJobType();

    /**
     * Returns the maximum number of workers this building can support.
     *
     * @return the max worker count
     */
    int getMaxWorkers();

    /**
     * Returns the list of citizen UUIDs currently assigned as workers.
     *
     * @return unmodifiable list of assigned worker UUIDs
     */
    List<UUID> getAssignedWorkers();

    /**
     * Attempts to assign a citizen as a worker at this building.
     *
     * @param citizenId the UUID of the citizen to assign
     * @return true if the citizen was successfully assigned, false if full or already assigned
     */
    boolean assignWorker(UUID citizenId);

    /**
     * Removes a citizen from this building's worker roster.
     *
     * @param citizenId the UUID of the citizen to remove
     */
    void removeWorker(UUID citizenId);
}
