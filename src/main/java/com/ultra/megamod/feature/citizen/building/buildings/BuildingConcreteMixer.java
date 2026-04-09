package com.ultra.megamod.feature.citizen.building.buildings;

import com.ultra.megamod.feature.citizen.building.AbstractBuilding;
import com.ultra.megamod.feature.citizen.building.module.CraftingBuildingModule;
import com.ultra.megamod.feature.citizen.building.module.WorkerBuildingModule;

import com.ultra.megamod.feature.citizen.data.CitizenJob;

/**
 * Concrete Mixer building implementation.
 */
public class BuildingConcreteMixer extends AbstractBuilding {

    @Override
    public String getBuildingId() {
        return "concrete_mixer";
    }

    @Override
    public String getDisplayName() {
        return "Concrete Mixer";
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    protected void registerModules() {
        addModule(new WorkerBuildingModule(CitizenJob.CONCRETE_MIXER, 1));
        addModule(new CraftingBuildingModule("concrete_mixer", 10));
    }
}
