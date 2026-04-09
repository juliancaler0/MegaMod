package com.ultra.megamod.feature.citizen.building.buildings;

import com.ultra.megamod.feature.citizen.building.AbstractBuilding;

/**
 * Mystical Site — provides colony-wide magical bonuses. No worker.
 */
public class BuildingMysticalSite extends AbstractBuilding {

    @Override
    public String getBuildingId() {
        return "mystical_site";
    }

    @Override
    public String getDisplayName() {
        return "Mystical Site";
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    protected void registerModules() {
        // No worker — passive colony-wide buff building
    }
}
