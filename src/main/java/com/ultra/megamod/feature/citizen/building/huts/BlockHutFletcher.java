package com.ultra.megamod.feature.citizen.building.huts;

import com.mojang.serialization.MapCodec;
import com.ultra.megamod.feature.citizen.building.AbstractBlockHut;

/**
 * Hut block for the fletcher building.
 */
public class BlockHutFletcher extends AbstractBlockHut<BlockHutFletcher> {

    public static final MapCodec<BlockHutFletcher> CODEC = simpleCodec(BlockHutFletcher::new);

    public BlockHutFletcher(Properties props) {
        super(props);
    }

    @Override
    protected MapCodec<BlockHutFletcher> codec() {
        return CODEC;
    }

    @Override
    public String getBuildingId() {
        return "fletcher";
    }
}
