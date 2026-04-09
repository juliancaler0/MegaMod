package com.ultra.megamod.feature.citizen.building.buildings;

import com.ultra.megamod.feature.citizen.building.AbstractBuilding;
import com.ultra.megamod.feature.citizen.building.module.CraftingBuildingModule;
import com.ultra.megamod.feature.citizen.building.module.WorkerBuildingModule;

import com.ultra.megamod.feature.citizen.data.CitizenJob;

/**
 * Crusher building implementation.
 */
public class BuildingCrusher extends AbstractBuilding {

    @Override
    public String getBuildingId() {
        return "crusher";
    }

    @Override
    public String getDisplayName() {
        return "Crusher";
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    protected void registerModules() {
        addModule(new WorkerBuildingModule(CitizenJob.CRUSHER, 1));
        addModule(new CraftingBuildingModule("crusher", 10));
    }
}
