package com.ultra.megamod.feature.citizen.building.buildings;

import com.ultra.megamod.feature.citizen.building.AbstractBuilding;
import com.ultra.megamod.feature.citizen.building.module.CraftingBuildingModule;
import com.ultra.megamod.feature.citizen.building.module.WorkerBuildingModule;

import com.ultra.megamod.feature.citizen.data.CitizenJob;

/**
 * Kitchen building implementation.
 */
public class BuildingKitchen extends AbstractBuilding {

    @Override
    public String getBuildingId() {
        return "kitchen";
    }

    @Override
    public String getDisplayName() {
        return "Kitchen";
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    protected void registerModules() {
        addModule(new WorkerBuildingModule(CitizenJob.CHEF, 1));
        addModule(new CraftingBuildingModule("kitchen", 10));
    }
}
