package com.ultra.megamod.feature.citizen.building.huts;

import com.mojang.serialization.MapCodec;
import com.ultra.megamod.feature.citizen.building.AbstractBlockHut;

/**
 * Hut block for the cowboy building.
 */
public class BlockHutCowboy extends AbstractBlockHut<BlockHutCowboy> {

    public static final MapCodec<BlockHutCowboy> CODEC = simpleCodec(BlockHutCowboy::new);

    public BlockHutCowboy(Properties props) {
        super(props);
    }

    @Override
    protected MapCodec<BlockHutCowboy> codec() {
        return CODEC;
    }

    @Override
    public String getBuildingId() {
        return "cowboy";
    }
}
