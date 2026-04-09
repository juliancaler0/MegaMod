package com.ultra.megamod.feature.citizen.building.buildings;

import com.ultra.megamod.feature.citizen.building.AbstractBuilding;
import com.ultra.megamod.feature.citizen.building.module.WorkerBuildingModule;

import com.ultra.megamod.feature.citizen.data.CitizenJob;

/**
 * Nether Mine building implementation.
 */
public class BuildingNetherWorker extends AbstractBuilding {

    @Override
    public String getBuildingId() {
        return "nether_worker";
    }

    @Override
    public String getDisplayName() {
        return "Nether Mine";
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    protected void registerModules() {
        addModule(new WorkerBuildingModule(CitizenJob.NETHER_MINER, 1));
    }
}
