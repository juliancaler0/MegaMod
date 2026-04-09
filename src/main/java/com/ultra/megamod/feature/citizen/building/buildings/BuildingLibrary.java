package com.ultra.megamod.feature.citizen.building.buildings;

import com.ultra.megamod.feature.citizen.building.AbstractBuilding;
import com.ultra.megamod.feature.citizen.building.module.WorkerBuildingModule;
import com.ultra.megamod.feature.citizen.data.CitizenJob;

/**
 * Library — pupils study here to level up skills.
 */
public class BuildingLibrary extends AbstractBuilding {

    @Override
    public String getBuildingId() {
        return "library";
    }

    @Override
    public String getDisplayName() {
        return "Library";
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    protected void registerModules() {
        addModule(new WorkerBuildingModule(CitizenJob.PUPIL, 2));
    }
}
