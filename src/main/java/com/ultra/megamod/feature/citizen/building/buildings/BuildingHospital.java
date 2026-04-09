package com.ultra.megamod.feature.citizen.building.buildings;

import com.ultra.megamod.feature.citizen.building.AbstractBuilding;
import com.ultra.megamod.feature.citizen.building.module.WorkerBuildingModule;
import com.ultra.megamod.feature.citizen.data.CitizenJob;

/**
 * Hospital — healer cures sick and injured citizens.
 */
public class BuildingHospital extends AbstractBuilding {

    @Override
    public String getBuildingId() {
        return "hospital";
    }

    @Override
    public String getDisplayName() {
        return "Hospital";
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    protected void registerModules() {
        addModule(new WorkerBuildingModule(CitizenJob.HEALER, 1));
    }
}
