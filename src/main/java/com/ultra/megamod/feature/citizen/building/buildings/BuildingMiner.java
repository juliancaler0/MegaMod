package com.ultra.megamod.feature.citizen.building.buildings;

import com.ultra.megamod.feature.citizen.building.AbstractBuilding;
import com.ultra.megamod.feature.citizen.building.module.WorkerBuildingModule;

import com.ultra.megamod.feature.citizen.data.CitizenJob;

/**
 * Mine building implementation.
 */
public class BuildingMiner extends AbstractBuilding {

    @Override
    public String getBuildingId() {
        return "miner";
    }

    @Override
    public String getDisplayName() {
        return "Mine";
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    protected void registerModules() {
        addModule(new WorkerBuildingModule(CitizenJob.MINER, 1));
    }
}
