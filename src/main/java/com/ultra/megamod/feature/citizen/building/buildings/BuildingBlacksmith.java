package com.ultra.megamod.feature.citizen.building.buildings;

import com.ultra.megamod.feature.citizen.building.AbstractBuilding;
import com.ultra.megamod.feature.citizen.building.module.CraftingBuildingModule;
import com.ultra.megamod.feature.citizen.building.module.WorkerBuildingModule;

import com.ultra.megamod.feature.citizen.data.CitizenJob;

/**
 * Blacksmith building implementation.
 */
public class BuildingBlacksmith extends AbstractBuilding {

    @Override
    public String getBuildingId() {
        return "blacksmith";
    }

    @Override
    public String getDisplayName() {
        return "Blacksmith";
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    protected void registerModules() {
        addModule(new WorkerBuildingModule(CitizenJob.BLACKSMITH, 1));
        addModule(new CraftingBuildingModule("blacksmith", 15));
    }
}
