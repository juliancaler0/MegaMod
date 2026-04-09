package com.ultra.megamod.feature.citizen.building.buildings;

import com.ultra.megamod.feature.citizen.building.AbstractBuilding;
import com.ultra.megamod.feature.citizen.building.module.WorkerBuildingModule;

import com.ultra.megamod.feature.citizen.data.CitizenJob;

/**
 * Cowboy's Ranch building implementation.
 */
public class BuildingCowboy extends AbstractBuilding {

    @Override
    public String getBuildingId() {
        return "cowboy";
    }

    @Override
    public String getDisplayName() {
        return "Cowboy's Ranch";
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    protected void registerModules() {
        addModule(new WorkerBuildingModule(CitizenJob.CATTLE_FARMER, 1));
    }
}
