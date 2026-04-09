package com.ultra.megamod.feature.citizen.building.huts;

import com.mojang.serialization.MapCodec;
import com.ultra.megamod.feature.citizen.building.AbstractBlockHut;

/**
 * Hut block for the baker building.
 */
public class BlockHutBaker extends AbstractBlockHut<BlockHutBaker> {

    public static final MapCodec<BlockHutBaker> CODEC = simpleCodec(BlockHutBaker::new);

    public BlockHutBaker(Properties props) {
        super(props);
    }

    @Override
    protected MapCodec<BlockHutBaker> codec() {
        return CODEC;
    }

    @Override
    public String getBuildingId() {
        return "baker";
    }
}
