package com.ultra.megamod.feature.citizen.building.buildings;

import com.ultra.megamod.feature.citizen.building.AbstractBuilding;
import com.ultra.megamod.feature.citizen.building.module.WorkerBuildingModule;

import com.ultra.megamod.feature.citizen.data.CitizenJob;

/**
 * Lumberjack building implementation.
 */
public class BuildingLumberjack extends AbstractBuilding {

    @Override
    public String getBuildingId() {
        return "lumberjack";
    }

    @Override
    public String getDisplayName() {
        return "Lumberjack";
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    protected void registerModules() {
        addModule(new WorkerBuildingModule(CitizenJob.LUMBERJACK, 1));
    }
}
