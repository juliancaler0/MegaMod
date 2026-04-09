package com.ultra.megamod.feature.citizen.building.buildings;

import com.ultra.megamod.feature.citizen.building.AbstractBuilding;
import com.ultra.megamod.feature.citizen.building.module.WorkerBuildingModule;
import com.ultra.megamod.feature.citizen.data.CitizenJob;

/**
 * School — a teacher instructs pupils.
 */
public class BuildingSchool extends AbstractBuilding {

    @Override
    public String getBuildingId() {
        return "school";
    }

    @Override
    public String getDisplayName() {
        return "School";
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    protected void registerModules() {
        addModule(new WorkerBuildingModule(CitizenJob.TEACHER, 1));
        addModule(new WorkerBuildingModule(CitizenJob.PUPIL, 2));
    }
}
