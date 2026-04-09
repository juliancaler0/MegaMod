package com.ultra.megamod.feature.citizen.building;

import net.minecraft.core.BlockPos;

/**
 * Interface for buildings that can be placed from schematics.
 * Provides the schematic name, level, style, and position needed
 * by the Builder citizen to construct or upgrade the building.
 */
public interface ISchematicProvider {

    /**
     * Returns the schematic name for this building (e.g., "residence", "baker", "miner").
     * Combined with style and level to form the full schematic path.
     *
     * @return the schematic name
     */
    String getSchematicName();

    /**
     * Returns the current building level (0 = unbuilt, 1-5 = built levels).
     *
     * @return the building level
     */
    int getBuildingLevel();

    /**
     * Sets the building level.
     *
     * @param level the new building level
     */
    void setBuildingLevel(int level);

    /**
     * Returns the style pack name for this building (e.g., "colonial", "medieval").
     * Determines which schematic variant is used.
     *
     * @return the style name
     */
    String getStyle();

    /**
     * Returns the world position of this building's hut block.
     *
     * @return the block position
     */
    BlockPos getPosition();
}
