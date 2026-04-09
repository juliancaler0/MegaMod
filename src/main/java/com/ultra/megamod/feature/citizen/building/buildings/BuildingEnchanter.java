package com.ultra.megamod.feature.citizen.building.buildings;

import com.ultra.megamod.feature.citizen.building.AbstractBuilding;
import com.ultra.megamod.feature.citizen.building.module.WorkerBuildingModule;

import com.ultra.megamod.feature.citizen.data.CitizenJob;

/**
 * Enchanter's Tower building implementation.
 */
public class BuildingEnchanter extends AbstractBuilding {

    @Override
    public String getBuildingId() {
        return "enchanter";
    }

    @Override
    public String getDisplayName() {
        return "Enchanter's Tower";
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    protected void registerModules() {
        addModule(new WorkerBuildingModule(CitizenJob.ENCHANTER, 1));
    }
}
