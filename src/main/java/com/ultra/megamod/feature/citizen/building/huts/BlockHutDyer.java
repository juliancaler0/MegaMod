package com.ultra.megamod.feature.citizen.building.huts;

import com.mojang.serialization.MapCodec;
import com.ultra.megamod.feature.citizen.building.AbstractBlockHut;

/**
 * Hut block for the dyer building.
 */
public class BlockHutDyer extends AbstractBlockHut<BlockHutDyer> {

    public static final MapCodec<BlockHutDyer> CODEC = simpleCodec(BlockHutDyer::new);

    public BlockHutDyer(Properties props) {
        super(props);
    }

    @Override
    protected MapCodec<BlockHutDyer> codec() {
        return CODEC;
    }

    @Override
    public String getBuildingId() {
        return "dyer";
    }
}
