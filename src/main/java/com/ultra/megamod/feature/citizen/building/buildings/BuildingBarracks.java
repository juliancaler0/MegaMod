package com.ultra.megamod.feature.citizen.building.buildings;

import com.ultra.megamod.feature.citizen.building.AbstractBuilding;

/**
 * Barracks building — houses guard towers but has no direct worker.
 * Guards are assigned to BarracksTower sub-buildings.
 *
 * Tower capacity scales by level: 1/2/3/4/4
 * Spy hiring system: spend gold to activate a spy for intel on raids.
 */
public class BuildingBarracks extends AbstractBuilding {

    /** Max towers allowed by building level (1-5). */
    private static final int[] MAX_TOWERS_BY_LEVEL = { 1, 2, 3, 4, 4 };

    /** Whether a spy is currently active (hired and deployed). */
    private boolean spyActive = false;

    /** Default spy hiring cost in gold ingots. */
    private static final int DEFAULT_SPY_GOLD_COST = 5;

    @Override
    public String getBuildingId() {
        return "barracks";
    }

    @Override
    public String getDisplayName() {
        return "Barracks";
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    protected void registerModules() {
        // No direct worker module — guards are assigned to BarracksTower sub-buildings
    }

    /**
     * Returns the maximum number of BarracksTower sub-buildings this barracks can support
     * based on its current building level.
     *
     * Level 1: 1 tower
     * Level 2: 2 towers
     * Level 3: 3 towers
     * Level 4: 4 towers
     * Level 5: 4 towers
     *
     * @return max tower count for current level
     */
    public int maxTowersByLevel() {
        int level = getBuildingLevel();
        if (level < 1) return 1;
        if (level > MAX_TOWERS_BY_LEVEL.length) return MAX_TOWERS_BY_LEVEL[MAX_TOWERS_BY_LEVEL.length - 1];
        return MAX_TOWERS_BY_LEVEL[level - 1];
    }

    /**
     * Hires a spy by spending gold ingots. The spy provides intel on incoming raids,
     * giving the colony advance warning and information about raider composition.
     *
     * @param goldCost the cost in gold ingots to hire the spy
     * @return true if the spy was successfully hired (caller must verify gold was consumed)
     */
    public boolean hireSpies(int goldCost) {
        if (spyActive) {
            return false; // Already have an active spy
        }
        if (goldCost < DEFAULT_SPY_GOLD_COST) {
            return false; // Insufficient payment
        }
        spyActive = true;
        return true;
    }

    /**
     * Dismisses the currently active spy.
     */
    public void dismissSpy() {
        spyActive = false;
    }

    /**
     * Returns whether a spy is currently active for this barracks.
     * An active spy reveals raid information (composition, timing, direction).
     */
    public boolean isSpyActive() {
        return spyActive;
    }

    /**
     * Sets the spy active status directly (used for persistence loading).
     */
    public void setSpyActive(boolean active) {
        this.spyActive = active;
    }
}
