package com.ultra.megamod.feature.citizen.building.huts;

import com.mojang.serialization.MapCodec;
import com.ultra.megamod.feature.citizen.building.AbstractBlockHut;

/**
 * Hut block for the smeltery building.
 */
public class BlockHutSmeltery extends AbstractBlockHut<BlockHutSmeltery> {

    public static final MapCodec<BlockHutSmeltery> CODEC = simpleCodec(BlockHutSmeltery::new);

    public BlockHutSmeltery(Properties props) {
        super(props);
    }

    @Override
    protected MapCodec<BlockHutSmeltery> codec() {
        return CODEC;
    }

    @Override
    public String getBuildingId() {
        return "smeltery";
    }
}
