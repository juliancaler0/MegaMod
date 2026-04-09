package com.ultra.megamod.feature.citizen.building.huts;

import com.mojang.serialization.MapCodec;
import com.ultra.megamod.feature.citizen.building.AbstractBlockHut;

/**
 * Hut block for the beekeeper building.
 */
public class BlockHutBeekeeper extends AbstractBlockHut<BlockHutBeekeeper> {

    public static final MapCodec<BlockHutBeekeeper> CODEC = simpleCodec(BlockHutBeekeeper::new);

    public BlockHutBeekeeper(Properties props) {
        super(props);
    }

    @Override
    protected MapCodec<BlockHutBeekeeper> codec() {
        return CODEC;
    }

    @Override
    public String getBuildingId() {
        return "beekeeper";
    }
}
