package com.ultra.megamod.feature.citizen.building.huts;

import com.mojang.serialization.MapCodec;
import com.ultra.megamod.feature.citizen.building.AbstractBlockHut;

/**
 * Hut block for the shepherd building.
 */
public class BlockHutShepherd extends AbstractBlockHut<BlockHutShepherd> {

    public static final MapCodec<BlockHutShepherd> CODEC = simpleCodec(BlockHutShepherd::new);

    public BlockHutShepherd(Properties props) {
        super(props);
    }

    @Override
    protected MapCodec<BlockHutShepherd> codec() {
        return CODEC;
    }

    @Override
    public String getBuildingId() {
        return "shepherd";
    }
}
