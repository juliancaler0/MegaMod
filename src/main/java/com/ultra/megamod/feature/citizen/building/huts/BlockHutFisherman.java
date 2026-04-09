package com.ultra.megamod.feature.citizen.building.huts;

import com.mojang.serialization.MapCodec;
import com.ultra.megamod.feature.citizen.building.AbstractBlockHut;

/**
 * Hut block for the fisherman building.
 */
public class BlockHutFisherman extends AbstractBlockHut<BlockHutFisherman> {

    public static final MapCodec<BlockHutFisherman> CODEC = simpleCodec(BlockHutFisherman::new);

    public BlockHutFisherman(Properties props) {
        super(props);
    }

    @Override
    protected MapCodec<BlockHutFisherman> codec() {
        return CODEC;
    }

    @Override
    public String getBuildingId() {
        return "fisherman";
    }
}
