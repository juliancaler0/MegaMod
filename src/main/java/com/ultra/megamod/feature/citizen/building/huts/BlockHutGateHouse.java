package com.ultra.megamod.feature.citizen.building.huts;

import com.mojang.serialization.MapCodec;
import com.ultra.megamod.feature.citizen.building.AbstractBlockHut;

/**
 * Hut block for the gate_house building.
 */
public class BlockHutGateHouse extends AbstractBlockHut<BlockHutGateHouse> {

    public static final MapCodec<BlockHutGateHouse> CODEC = simpleCodec(BlockHutGateHouse::new);

    public BlockHutGateHouse(Properties props) {
        super(props);
    }

    @Override
    protected MapCodec<BlockHutGateHouse> codec() {
        return CODEC;
    }

    @Override
    public String getBuildingId() {
        return "gate_house";
    }
}
