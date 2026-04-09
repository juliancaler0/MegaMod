package com.ultra.megamod.feature.citizen.building.buildings;

import com.ultra.megamod.feature.citizen.building.AbstractBuilding;
import com.ultra.megamod.feature.citizen.building.module.CraftingBuildingModule;
import com.ultra.megamod.feature.citizen.building.module.WorkerBuildingModule;

import com.ultra.megamod.feature.citizen.data.CitizenJob;

/**
 * Sifter building implementation.
 */
public class BuildingSifter extends AbstractBuilding {

    @Override
    public String getBuildingId() {
        return "sifter";
    }

    @Override
    public String getDisplayName() {
        return "Sifter";
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    protected void registerModules() {
        addModule(new WorkerBuildingModule(CitizenJob.SIFTER, 1));
        addModule(new CraftingBuildingModule("sifter", 10));
    }
}
