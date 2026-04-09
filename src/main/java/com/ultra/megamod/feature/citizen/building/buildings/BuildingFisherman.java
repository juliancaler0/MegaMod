package com.ultra.megamod.feature.citizen.building.buildings;

import com.ultra.megamod.feature.citizen.building.AbstractBuilding;
import com.ultra.megamod.feature.citizen.building.module.WorkerBuildingModule;

import com.ultra.megamod.feature.citizen.data.CitizenJob;

/**
 * Fisherman's Hut building implementation.
 */
public class BuildingFisherman extends AbstractBuilding {

    @Override
    public String getBuildingId() {
        return "fisherman";
    }

    @Override
    public String getDisplayName() {
        return "Fisherman's Hut";
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    protected void registerModules() {
        addModule(new WorkerBuildingModule(CitizenJob.FISHERMAN, 1));
    }
}
