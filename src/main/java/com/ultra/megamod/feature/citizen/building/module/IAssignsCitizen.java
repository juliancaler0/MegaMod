package com.ultra.megamod.feature.citizen.building.module;

import java.util.List;
import java.util.UUID;

/**
 * Module interface for residential buildings that house citizens.
 * Buildings with this module serve as homes where citizens sleep and live.
 */
public interface IAssignsCitizen extends IBuildingModule {

    /**
     * Returns the maximum number of residents this building can house.
     *
     * @return the max resident count
     */
    int getMaxResidents();

    /**
     * Returns the list of citizen UUIDs currently assigned as residents.
     *
     * @return unmodifiable list of resident UUIDs
     */
    List<UUID> getResidents();

    /**
     * Attempts to assign a citizen as a resident of this building.
     *
     * @param citizenId the UUID of the citizen to assign
     * @return true if the citizen was successfully assigned, false if full or already assigned
     */
    boolean assignResident(UUID citizenId);

    /**
     * Removes a citizen from this building's resident list.
     *
     * @param citizenId the UUID of the citizen to remove
     */
    void removeResident(UUID citizenId);
}
