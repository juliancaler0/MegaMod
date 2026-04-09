package com.ultra.megamod.feature.citizen.building.buildings;

import com.ultra.megamod.feature.citizen.building.AbstractBuilding;
import com.ultra.megamod.feature.citizen.building.module.WorkerBuildingModule;

import com.ultra.megamod.feature.citizen.data.CitizenJob;

/**
 * Plantation building implementation.
 */
public class BuildingPlantation extends AbstractBuilding {

    @Override
    public String getBuildingId() {
        return "plantation";
    }

    @Override
    public String getDisplayName() {
        return "Plantation";
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    protected void registerModules() {
        addModule(new WorkerBuildingModule(CitizenJob.PLANTER, 1));
    }
}
