package com.ultra.megamod.feature.citizen.building.huts;

import com.mojang.serialization.MapCodec;
import com.ultra.megamod.feature.citizen.building.AbstractBlockHut;

/**
 * Hut block for the warehouse building.
 */
public class BlockHutWarehouse extends AbstractBlockHut<BlockHutWarehouse> {

    public static final MapCodec<BlockHutWarehouse> CODEC = simpleCodec(BlockHutWarehouse::new);

    public BlockHutWarehouse(Properties props) {
        super(props);
    }

    @Override
    protected MapCodec<BlockHutWarehouse> codec() {
        return CODEC;
    }

    @Override
    public String getBuildingId() {
        return "warehouse";
    }
}
