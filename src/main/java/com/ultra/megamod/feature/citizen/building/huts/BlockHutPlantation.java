package com.ultra.megamod.feature.citizen.building.huts;

import com.mojang.serialization.MapCodec;
import com.ultra.megamod.feature.citizen.building.AbstractBlockHut;

/**
 * Hut block for the plantation building.
 */
public class BlockHutPlantation extends AbstractBlockHut<BlockHutPlantation> {

    public static final MapCodec<BlockHutPlantation> CODEC = simpleCodec(BlockHutPlantation::new);

    public BlockHutPlantation(Properties props) {
        super(props);
    }

    @Override
    protected MapCodec<BlockHutPlantation> codec() {
        return CODEC;
    }

    @Override
    public String getBuildingId() {
        return "plantation";
    }
}
