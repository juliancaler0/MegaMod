package com.ultra.megamod.feature.citizen.building.huts;

import com.mojang.serialization.MapCodec;
import com.ultra.megamod.feature.citizen.building.AbstractBlockHut;

/**
 * Hut block for the university building.
 */
public class BlockHutUniversity extends AbstractBlockHut<BlockHutUniversity> {

    public static final MapCodec<BlockHutUniversity> CODEC = simpleCodec(BlockHutUniversity::new);

    public BlockHutUniversity(Properties props) {
        super(props);
    }

    @Override
    protected MapCodec<BlockHutUniversity> codec() {
        return CODEC;
    }

    @Override
    public String getBuildingId() {
        return "university";
    }
}
