package com.ultra.megamod.feature.citizen.building.huts;

import com.mojang.serialization.MapCodec;
import com.ultra.megamod.feature.citizen.building.AbstractBlockHut;

/**
 * Hut block for the stone_smeltery building.
 */
public class BlockHutStoneSmeltery extends AbstractBlockHut<BlockHutStoneSmeltery> {

    public static final MapCodec<BlockHutStoneSmeltery> CODEC = simpleCodec(BlockHutStoneSmeltery::new);

    public BlockHutStoneSmeltery(Properties props) {
        super(props);
    }

    @Override
    protected MapCodec<BlockHutStoneSmeltery> codec() {
        return CODEC;
    }

    @Override
    public String getBuildingId() {
        return "stone_smeltery";
    }
}
