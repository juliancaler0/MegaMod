package com.ultra.megamod.feature.citizen.building.buildings;

import com.ultra.megamod.feature.citizen.building.AbstractBuilding;
import com.ultra.megamod.feature.citizen.building.module.WorkerBuildingModule;
import com.ultra.megamod.feature.citizen.data.CitizenJob;

/**
 * Archery range — trains archers.
 */
public class BuildingArchery extends AbstractBuilding {

    @Override
    public String getBuildingId() {
        return "archery";
    }

    @Override
    public String getDisplayName() {
        return "Archery Range";
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    protected void registerModules() {
        addModule(new WorkerBuildingModule(CitizenJob.ARCHER_TRAINING, 1));
    }
}
