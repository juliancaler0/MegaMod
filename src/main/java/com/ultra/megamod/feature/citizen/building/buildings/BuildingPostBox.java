package com.ultra.megamod.feature.citizen.building.buildings;

import com.ultra.megamod.feature.citizen.building.AbstractBuilding;

/**
 * Post Box — simple request interface for the colony. Max level 1.
 * Players can submit item requests through this building.
 */
public class BuildingPostBox extends AbstractBuilding {

    @Override
    public String getBuildingId() {
        return "post_box";
    }

    @Override
    public String getDisplayName() {
        return "Post Box";
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    protected void registerModules() {
        // No worker — simple request interface
    }
}
