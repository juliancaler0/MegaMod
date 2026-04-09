package com.ultra.megamod.feature.citizen.building.buildings;

import com.ultra.megamod.feature.citizen.building.AbstractBuilding;
import com.ultra.megamod.feature.citizen.building.module.WorkerBuildingModule;

import com.ultra.megamod.feature.citizen.data.CitizenJob;

/**
 * Shepherd's Hut building implementation.
 */
public class BuildingShepherd extends AbstractBuilding {

    @Override
    public String getBuildingId() {
        return "shepherd";
    }

    @Override
    public String getDisplayName() {
        return "Shepherd's Hut";
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    protected void registerModules() {
        addModule(new WorkerBuildingModule(CitizenJob.SHEPHERD, 1));
    }
}
