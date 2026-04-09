package com.ultra.megamod.feature.citizen.building.huts;

import com.mojang.serialization.MapCodec;
import com.ultra.megamod.feature.citizen.building.AbstractBlockHut;

/**
 * Hut block for the sawmill building.
 */
public class BlockHutSawmill extends AbstractBlockHut<BlockHutSawmill> {

    public static final MapCodec<BlockHutSawmill> CODEC = simpleCodec(BlockHutSawmill::new);

    public BlockHutSawmill(Properties props) {
        super(props);
    }

    @Override
    protected MapCodec<BlockHutSawmill> codec() {
        return CODEC;
    }

    @Override
    public String getBuildingId() {
        return "sawmill";
    }
}
