package com.ultra.megamod.feature.citizen.building.huts;

import com.mojang.serialization.MapCodec;
import com.ultra.megamod.feature.citizen.building.AbstractBlockHut;

/**
 * Hut block for the florist building.
 */
public class BlockHutFlorist extends AbstractBlockHut<BlockHutFlorist> {

    public static final MapCodec<BlockHutFlorist> CODEC = simpleCodec(BlockHutFlorist::new);

    public BlockHutFlorist(Properties props) {
        super(props);
    }

    @Override
    protected MapCodec<BlockHutFlorist> codec() {
        return CODEC;
    }

    @Override
    public String getBuildingId() {
        return "florist";
    }
}
