package com.ultra.megamod.feature.citizen.building.buildings;

import com.ultra.megamod.feature.citizen.building.AbstractBuilding;
import com.ultra.megamod.feature.citizen.building.module.WorkerBuildingModule;

import com.ultra.megamod.feature.citizen.data.CitizenJob;

/**
 * Apiary building implementation.
 */
public class BuildingBeekeeper extends AbstractBuilding {

    @Override
    public String getBuildingId() {
        return "beekeeper";
    }

    @Override
    public String getDisplayName() {
        return "Apiary";
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    protected void registerModules() {
        addModule(new WorkerBuildingModule(CitizenJob.BEEKEEPER, 1));
    }
}
