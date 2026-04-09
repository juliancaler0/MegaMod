package com.ultra.megamod.feature.citizen.building.huts;

import com.mojang.serialization.MapCodec;
import com.ultra.megamod.feature.citizen.building.AbstractBlockHut;

/**
 * Hut block for the farmer building.
 */
public class BlockHutFarmer extends AbstractBlockHut<BlockHutFarmer> {

    public static final MapCodec<BlockHutFarmer> CODEC = simpleCodec(BlockHutFarmer::new);

    public BlockHutFarmer(Properties props) {
        super(props);
    }

    @Override
    protected MapCodec<BlockHutFarmer> codec() {
        return CODEC;
    }

    @Override
    public String getBuildingId() {
        return "farmer";
    }
}
