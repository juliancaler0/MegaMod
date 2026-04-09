package com.ultra.megamod.feature.citizen.building.buildings;

import com.ultra.megamod.feature.citizen.building.AbstractBuilding;
import com.ultra.megamod.feature.citizen.building.module.WorkerBuildingModule;

import com.ultra.megamod.feature.citizen.data.CitizenJob;

/**
 * Farm building implementation.
 */
public class BuildingFarmer extends AbstractBuilding {

    @Override
    public String getBuildingId() {
        return "farmer";
    }

    @Override
    public String getDisplayName() {
        return "Farm";
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    protected void registerModules() {
        addModule(new WorkerBuildingModule(CitizenJob.FARMER, 1));
    }
}
