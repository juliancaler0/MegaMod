package com.ultra.megamod.feature.citizen.building.huts;

import com.mojang.serialization.MapCodec;
import com.ultra.megamod.feature.citizen.building.AbstractBlockHut;

/**
 * Hut block for the nether_worker building.
 */
public class BlockHutNetherWorker extends AbstractBlockHut<BlockHutNetherWorker> {

    public static final MapCodec<BlockHutNetherWorker> CODEC = simpleCodec(BlockHutNetherWorker::new);

    public BlockHutNetherWorker(Properties props) {
        super(props);
    }

    @Override
    protected MapCodec<BlockHutNetherWorker> codec() {
        return CODEC;
    }

    @Override
    public String getBuildingId() {
        return "nether_worker";
    }
}
