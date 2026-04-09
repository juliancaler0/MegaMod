package com.ultra.megamod.feature.citizen.request;

import com.ultra.megamod.feature.citizen.building.TileEntityColonyBuilding;
import net.minecraft.core.BlockPos;

/**
 * An {@link IRequester} that represents a colony building requesting materials.
 * Used by {@link com.ultra.megamod.feature.citizen.building.ProductionManager}
 * to auto-create delivery requests when a building needs ingredients for crafting.
 */
public class BuildingRequester implements IRequester {

    private final IToken requesterId;
    private final String buildingId;
    private final BlockPos position;

    /**
     * Creates a requester for the given colony building tile entity.
     *
     * @param tile the colony building tile entity
     */
    public BuildingRequester(TileEntityColonyBuilding tile) {
        this.requesterId = new StandardToken();
        this.buildingId = tile.getBuildingId() != null ? tile.getBuildingId() : "unknown";
        this.position = tile.getBlockPos();
    }

    @Override
    public IToken getRequesterId() {
        return requesterId;
    }

    @Override
    public String getRequesterName() {
        return "Building: " + buildingId;
    }

    @Override
    public BlockPos getRequesterPosition() {
        return position;
    }
}
