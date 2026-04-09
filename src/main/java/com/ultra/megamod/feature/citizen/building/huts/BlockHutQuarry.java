package com.ultra.megamod.feature.citizen.building.huts;

import com.mojang.serialization.MapCodec;
import com.ultra.megamod.feature.citizen.building.AbstractBlockHut;

/**
 * Hut block for the quarry building.
 */
public class BlockHutQuarry extends AbstractBlockHut<BlockHutQuarry> {

    public static final MapCodec<BlockHutQuarry> CODEC = simpleCodec(BlockHutQuarry::new);

    public BlockHutQuarry(Properties props) {
        super(props);
    }

    @Override
    protected MapCodec<BlockHutQuarry> codec() {
        return CODEC;
    }

    @Override
    public String getBuildingId() {
        return "quarry";
    }
}
