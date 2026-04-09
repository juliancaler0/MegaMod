package com.ultra.megamod.feature.citizen.building.huts;

import com.mojang.serialization.MapCodec;
import com.ultra.megamod.feature.citizen.building.AbstractBlockHut;

/**
 * Hut block for the miner building.
 */
public class BlockHutMiner extends AbstractBlockHut<BlockHutMiner> {

    public static final MapCodec<BlockHutMiner> CODEC = simpleCodec(BlockHutMiner::new);

    public BlockHutMiner(Properties props) {
        super(props);
    }

    @Override
    protected MapCodec<BlockHutMiner> codec() {
        return CODEC;
    }

    @Override
    public String getBuildingId() {
        return "miner";
    }
}
