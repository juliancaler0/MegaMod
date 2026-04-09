package com.ultra.megamod.feature.citizen.building.buildings;

import com.ultra.megamod.feature.citizen.building.AbstractBuilding;
import com.ultra.megamod.feature.citizen.building.module.WorkerBuildingModule;

import com.ultra.megamod.feature.citizen.data.CitizenJob;

/**
 * Quarry building implementation.
 */
public class BuildingQuarry extends AbstractBuilding {

    @Override
    public String getBuildingId() {
        return "quarry";
    }

    @Override
    public String getDisplayName() {
        return "Quarry";
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    protected void registerModules() {
        addModule(new WorkerBuildingModule(CitizenJob.QUARRIER, 1));
    }
}
